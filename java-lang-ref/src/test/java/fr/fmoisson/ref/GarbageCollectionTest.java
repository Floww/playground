package fr.fmoisson.ref;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.lang.ref.*;

/**
 * Test of package java.lang.ref.<br />
 * <br />
 * Launch this test with "-Xmx32m -verbose:gc"
 */
public class GarbageCollectionTest {
    @Rule
    public TestRule watcher = new TestWatcher() {
        protected void starting(Description description) {
            System.out.println("\n@@@ Starting test: " + description.getMethodName() + "\n");
        }

        @Override
        protected void finished(Description description) {
            System.out.println("\n@@@ Finished test: " + description.getMethodName() + "\n");
        }
    };

    /**
     * An object is softly reachable if it is not strongly reachable but can be reached by traversing a soft
     * reference.<br />
     * In other words, SoftReferences should be cleared before an OutOfMemoryError is thrown.<br />
     * For this, there are typically collect at a full gc for allocation failure.
     */
    @Test
    public void testSoftReference() throws InterruptedException {
        // creating a 5MB soft reference
        TestObject soft5MB = new TestObject(5);
        SoftReference r = new SoftReference(soft5MB);

        // release object
        soft5MB = null;

        // showing value
        System.out.println("soft5M = " + soft5MB + " but r.get() = " + r.get());

        // force a gc and let some time (finalizer thread)
        System.out.println("> Force GC");
        //System.gc();
        Thread.sleep(50);

        // check
        Assert.assertNotNull(r.get());
        System.out.println("soft5M = " + soft5MB + " but r.get() is still = " + r.get());

        // rate that triggered the full gc for allocation failure
        double rateFullGc = -1;

        // stress heap pool with large object collectable at each iteration
        for (double rate = 0.40; ; rate += 0.005) {
            int allocSize = (int) (Runtime.getRuntime().maxMemory() * rate);

            try {
                byte[] alloc = new byte[allocSize];

                // some time (finalizer thread)
                Thread.sleep(50);

                if (r.get() == null && rateFullGc == -1) {
                    rateFullGc = rate;
                    System.out.println("> Soft reference was collected at rate " + rate + " to prevent OutOfMemory");
                }

            } catch (OutOfMemoryError e) {
                Assert.assertNull(r.get());
                System.out.println("> OutOfMemory at rate " + rate);
                System.out.println("> Soft reference = " + r.get() + ", collected at rate " + rateFullGc);
                break;
            }
        }
    }

    /**
     * An object is weakly reachable if it is neither strongly nor softly reachable but can be reached by
     * traversing a weak reference. When the weak references to a weakly-reachable object are cleared, the
     * object becomes eligible for finalization.<br />
     * In other words, SoftReferences should be cleared before an OutOfMemoryError is thrown.<br />
     */
    @Test
    public void testWeakReference() throws InterruptedException {
        // creating a 5MB weak reference
        TestObject weak5MB = new TestObject(5);
        WeakReference r = new WeakReference(weak5MB);

        // release object
        weak5MB = null;

        // showing value
        System.out.println("weak5MB = " + weak5MB + " but r.get() = " + r.get());

        // force a gc and let some time (finalizer thread)
        System.out.println("> Force GC");
        System.gc();
        Thread.sleep(50);

        // check
        Assert.assertNull(r.get());
        System.out.println("weak5MB = " + weak5MB + " but r.get() is now = " + r.get());
    }

    /**
     * In practice these are rarely used, phantom references are the most tenuous of all reference types:
     * calling get will always return null. With it, we can know when an object has been removed collected.
     * It can replace the finalization mechanism which has the drawback of resurrecting the object in some cases.
     */
    @Test
    public void testPhantomReference() throws InterruptedException {
        // creating a 5MB phantom reference
        TestObject phantom5MB = new TestObject(5);
        ReferenceQueue rq = new ReferenceQueue();
        PhantomReference pr = new PhantomReference(phantom5MB, rq);

        // get() on phantom reference return null
        Assert.assertNull("get() should return null.", pr.get());

        // release object
        phantom5MB = null;

        // simple stress heap
        boolean enqueued = false;
        for(int i = 0; i < 50 && !enqueued; i++) {
            // waiting reference to be enqueued
            Reference ref = rq.remove(1000);

            if(ref != null) {
                System.out.println("> Object has been collected.");
                enqueued = true;
            }

            new String(new byte[1024 * 1024 * 5]);
        }

        Assert.assertTrue(enqueued);
    }
}

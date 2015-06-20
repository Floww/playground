package fr.fmoisson.ref;

/**
 * Created by fmoisson on 20/06/15.
 */
public final class TestObject {
    private byte[] pool;

    public TestObject(int nbMB) {
        this.pool = new byte[nbMB * 1024 * 1024];
    }

    public byte[] getPool() {
        return pool;
    }

    @Override
    protected void finalize() throws Throwable {
        System.out.println("> Object was finalized");
        super.finalize();
    }
}

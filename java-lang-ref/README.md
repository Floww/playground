# Synthesis

Just some tests of package java.lang.ref.

Javadocs : [https://docs.oracle.com/javase/8/docs/api/java/lang/ref/package-summary.html]()

# Explanations

Going from strongest to weakest, the different levels of reachability reflect the life cycle of an object.

They are operationally defined as follows:

* An object is strongly reachable if it can be reached by some thread without traversing any reference objects. A newly-created object is strongly reachable by the thread that created it.


* An object is softly reachable if it is not strongly reachable but can be reached by traversing a soft reference.


* An object is weakly reachable if it is neither strongly nor softly reachable but can be reached by traversing a weak reference. When the weak references to a weakly-reachable object are cleared, the object becomes eligible for finalization.


* An object is phantom reachable if it is neither strongly, softly, nor weakly reachable, it has been finalized, and some phantom reference refers to it.


* Finally, an object is unreachable, and therefore eligible for reclamation, when it is not reachable in any of the above ways.


package PetriSimplify;

import org.processmining.framework.packages.PackageManager.Canceller;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;

public class ReducePetriNet {
	public static void reduce(Petrinet petriNet, Canceller canceller) {

		boolean reduced = false;
		do {
			reduced = false;
			reduced |= PteriNetSimplificationRule1.reduce(petriNet, canceller);
			reduced |= PteriNetSimplificationRule2.reduce(petriNet, canceller);
			reduced |= PteriNetSimplificationRule31.reduce(petriNet, canceller);
			reduced |= PteriNetSimplificationRule32.reduce(petriNet, canceller);
			reduced |= PteriNetSimplificationRule4.reduce(petriNet, canceller);
			reduced |= PteriNetSimplificationRule5.reduce(petriNet, canceller);
			reduced |= PteriNetSimplificationRule7.reduce(petriNet, canceller);
//			reduced = false;
			if (canceller.isCancelled()) {
				return;
			}
		} while (reduced);
	}
}

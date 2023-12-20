package kmean;


import java.util.ArrayList;

import no.uib.cipr.matrix.sparse.SparseVector;

/**
 * Vector for keeping track of mapping from a trace to a certain perspective
 * 
 * @author B.F.A. Hompes
 *
 */
public class CaseVector extends SparseVector {

	private static final long serialVersionUID = -6420298104250532851L;

	public CaseVector(int size) {
		super(size);
	}

	private CaseVector(SparseVector vector) {
		super(vector);
	}

	public CaseVector copy() {
		return new CaseVector(super.copy());
	}

	public CaseVector toUnitVector() {
		if (isZeroVector())
			return this;

		CaseVector unit = copy();
		double magnitude = unit.norm1();
		for (int i = 0; i < unit.size; i++) {
			unit.set(i, unit.get(i) / magnitude);
		}
		return unit;
	}

	public boolean isZeroVector() {
		for (double element : this.getData())
			if (element != 0.0)
				return false;
		return true;
	}

	public static CaseVector concat(ArrayList<CaseVector> vectors) {
		int size = 0;

		for (CaseVector vector : vectors)
			size += vector.size();

		CaseVector combination = new CaseVector(size);

		int index = 0;
		for (CaseVector vector : vectors) {
			for (int i = 0; i < vector.size; i++)
				combination.set(index + i, vector.get(i));
			index += vector.size();
		}

		return combination;
	}

	/**
	 * Two tracevectors are equal if they have the same length and elements at
	 * corresponding positions.
	 * 
	 * (Is actually already handled by the base class SparseVector.)
	 */
	public boolean equals(Object obj) {
		if (!(obj instanceof CaseVector))
			return false;

		CaseVector vect = (CaseVector) obj;

		if (size() != vect.size())
			return false;

		for (int i = 0; i < this.size; i++) {
			if (get(i) != vect.get(i))
				return false;
		}

		return true;
	}

}
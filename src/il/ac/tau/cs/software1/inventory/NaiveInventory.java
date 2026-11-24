package il.ac.tau.cs.software1.inventory;

/*
 * A simple array-based Inventory (Question 6).
 */
public class NaiveInventory implements Inventory {

	private static final int INITIAL_CAPACITY = 100;

	private Collectible[] collArr;
	private int count;

	public NaiveInventory() {
		collArr = new Collectible[INITIAL_CAPACITY];
		count = 0;
	}

	@Override
	public void collect(Collectible collectible) {
		if (count == collArr.length) {
			Collectible[] bigger = new Collectible[collArr.length * 2];
			for (int i = 0; i < count; i++) {
				bigger[i] = collArr[i];
			}
			collArr = bigger;
		}
		collArr[count] = collectible;
		count++;
	}

	@Override
	public float getTotalWeight() {
		float total = 0.f;
		for (int i = 0; i < count; i++) {
			total += collArr[i].getWeight();
		}
		return total;
	}

	@Override
	public int getCurrentCount() {
		return count;
	}

	// Returns the array of Collectibles that is stored in the field of this class
	public Collectible[] getCollArr() {
		return collArr;
	}
}

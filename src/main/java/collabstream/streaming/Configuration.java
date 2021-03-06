package collabstream.streaming;

import java.io.Serializable;

public class Configuration implements Serializable {
	public final int numUsers, numItems, numLatent;
	public final int numUserBlocks, numItemBlocks;
	public final float userPenalty, itemPenalty, initialStepSize;
	public final int maxTrainingIters;
	public final String inputFilename, userOutputFilename, itemOutputFilename;
	public final long inputDelay; // delay between sending two training examples in milliseconds
	public final boolean debug;
	
	private final int smallUserBlockSize, smallItemBlockSize;
	private final int bigUserBlockSize, bigItemBlockSize;
	private final int numBigUserBlocks, numBigItemBlocks;
	private final int userBlockThreshold, itemBlockThreshold;
	
	public Configuration(int numUsers, int numItems, int numLatent, int numUserBlocks, int numItemBlocks,
						 float userPenalty, float itemPenalty, float initialStepSize, int maxTrainingIters,
						 String inputFilename, String userOutputFilename, String itemOutputFilename,
						 long inputDelay, boolean debug) {
		this.numUsers = numUsers;
		this.numItems = numItems;
		this.numLatent = numLatent;
		this.numUserBlocks = numUserBlocks;
		this.numItemBlocks = numItemBlocks;
		this.userPenalty = userPenalty;
		this.itemPenalty = itemPenalty;
		this.initialStepSize = initialStepSize;
		this.maxTrainingIters = maxTrainingIters;
		this.inputFilename = inputFilename;
		this.userOutputFilename = userOutputFilename;
		this.itemOutputFilename = itemOutputFilename;
		this.inputDelay = inputDelay;
		this.debug = debug;
		
		smallUserBlockSize = numUsers / numUserBlocks;
		bigUserBlockSize = smallUserBlockSize + 1;
		numBigUserBlocks = numUsers % numUserBlocks;
		userBlockThreshold = bigUserBlockSize * numBigUserBlocks; 
		
		smallItemBlockSize = numItems / numItemBlocks;
		bigItemBlockSize = smallItemBlockSize + 1;
		numBigItemBlocks = numItems % numItemBlocks;
		itemBlockThreshold = bigItemBlockSize * numBigItemBlocks;
	}
	
	public int getNumWorkers() {
		return numUserBlocks;
	}
	
	public int getNumProcesses() {
		// numUserBlocks (Worker) + numUserBlocks (MatrixStore) + numItemBlocks (MatrixStore) + 1 (Master) + 1 (RatingsSource)
		return 2*numUserBlocks + numItemBlocks + 2;
	}
	
	public int getUserBlockIdx(int userId) {
		if (userId < userBlockThreshold) {
			return userId / bigUserBlockSize; 
		} else {
			return numBigUserBlocks + (userId - userBlockThreshold) / smallUserBlockSize;
		}
	}
	
	public int getItemBlockIdx(int itemId) {
		if (itemId < itemBlockThreshold) {
			return itemId / bigItemBlockSize; 
		} else {
			return numBigItemBlocks + (itemId - itemBlockThreshold) / smallItemBlockSize;
		}
	}
	
	public int getUserBlockLength(int userBlockIdx) {
		return (userBlockIdx < numBigUserBlocks) ? bigUserBlockSize : smallUserBlockSize;
	}
	
	public int getItemBlockLength(int itemBlockIdx) {
		return (itemBlockIdx < numBigItemBlocks) ? bigItemBlockSize : smallItemBlockSize;
	}
	
	public int getUserBlockStart(int userBlockIdx) {
		if (userBlockIdx < numBigUserBlocks) {
			return bigUserBlockSize * userBlockIdx; 
		} else {
			return userBlockThreshold + (userBlockIdx - numBigUserBlocks) * smallUserBlockSize;
		}
	}
	
	public int getItemBlockStart(int itemBlockIdx) {
		if (itemBlockIdx < numBigItemBlocks) {
			return bigItemBlockSize * itemBlockIdx; 
		} else {
			return itemBlockThreshold + (itemBlockIdx - numBigItemBlocks) * smallItemBlockSize;
		}
	}
}
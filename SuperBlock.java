// SuperBlock.java

public class SuperBlock {
	
	private final int defaultInodeBlocks = 64;
	public int totalBlocks;
	public int inodeBlocks;
	public int freeList;


	public SuperBlock( int diskSize ) {
		byte[] superBlock = new byte[Disk.blockSize];
		SysLib.rawread( 0, superBlock );
		totalBlocks = SysLib.bytes2int( superBlock, 0 );
		inodeBlocks = SysLib.bytes2int( superBlock, 4 );
		freeList    = SysLib.bytes2int( superBlock, 8 );

		if ( totalBlocks == diskSize 
		&& inodeBlocks > 0 
		&& freeList >= 2 )
			return;

		else {
			totalBlocks = diskSize;
			format();
		}
	}

	void sync(){
		// **TODO** 
		// write totalBlocks, inodeBlocks, freelist to disk
        byte[] superBlock = new byte[Disk.blockSize];
        int offset = 0;
        SysLib.int2bytes(totalBlocks, superBlock, offset);
        SysLib.int2bytes(inodeBlocks, superBlock, offset + 4);
        SysLib.int2bytes(freeList, superBlock, offset + 8);
        SysLib.rawwrite(0, superBlock);
	}

	void format(){
		// **TODO**
		// initialize Inodes, free blocks
        inodeBlocks = defaultInodeBlocks;
        initFreeList();
        sync();
    }
	
	void format( int numBlocks ){
	
		if (numBlocks <= 0 || numBlocks >= totalBlocks) {
			inodeBlocks = 1; // need at least one...
		} else {
        	inodeBlocks = numBlocks; 
        }
        
        initFreeList();
        sync();
    }

	public int getFreeBlock(){
		// Check if there are any blocks left to acquire.
		if (freeList < 0 || freeList >= totalBlocks) {
			return -1;  // Failed to grab a free block.
		}
		
		// We want to grab the next block and read the beginning of the buffer
		// to get the next block number.  If successful, we return the number of
		// the current block, and set the FreeList to be the value pulled from
		// the block.
        int free_block_num = freeList;
        byte[] buffer = new byte[Disk.blockSize];
        SysLib.rawread(free_block_num, buffer);
        int next = SysLib.bytes2int(buffer, 0);
        freeList = next;
        return free_block_num;
	}


    // Takes the current pointer of freeList and sets it as the pointer of the
    // block "oldBlockNumber".  
    // 
    // freeList is then set to point to oldBlockNumber.
	public boolean returnBlock( int oldBlockNumber ) {
	
		// This *shouldn't* happen, but just in case...
		if (oldBlockNumber >= totalBlocks || oldBlockNumber < 0) {
			return false;
		}
		
		// Create a new buffer, write the freelist value to the start of the 
		// buffer, and then write the buffer to the oldBlock.  Set FreeList to
		// now point at the oldBlock.
        byte[] buffer = new byte[Disk.blockSize];
        int next = freeList;
        SysLib.int2bytes(next, buffer, 0);
        SysLib.rawwrite(oldBlockNumber, buffer);
        freeList = oldBlockNumber;
        return true;
	}

    void initFreeList() {
        // because returnBlock() adds the given block to the front/top of the
        // list, we need to add them in reverse order.  
        freeList = -1;
        int freeListEnd = totalBlocks - 1;
        int freeListStart = ((inodeBlocks * 32) / Disk.blockSize) + 1;
        if ((inodeBlocks * 32 ) % Disk.blockSize != 0) {
        	freeListStart += 1;
        }
        for (int i = freeListEnd; i > freeListStart; i--) {
      	    returnBlock(i);
        }
    }
}

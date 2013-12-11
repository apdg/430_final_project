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
			SysLib.cerr( "Formatting\n" );
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
		// **TODO**
		// initialize Inodes, free blocks
        inodeBlocks = numBlocks; // = numBlocks ???
        initFreeList();
        sync();
    }

	public int getFreeBlock(){
		// **TODO**
		// dequeue top block in freelist
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
	public boolean returnBlock( int oldBlockNumber ){
		// **TODO**
		// enqueue oldBlockNumber to top of freelist
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
        for (int i = freeListEnd; i > freeListStart; i++) {
            returnBlock(i);
        }
    }
}

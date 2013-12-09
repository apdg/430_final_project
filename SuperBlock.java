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
        freeBlocks = totalBlocks - inodeBlocks - 2; // superblock and directoryblock
    }
	
	void format( int numBlocks ){
		// **TODO**
		// initialize Inodes, free blocks
        inodeBlocks = numBlocks;
        freeBlocks = totalBlocks - inodeBlocks - 2; // superblock and directoryblock
	}

	public int getFreeBlock(){
		// **TODO**
		// dequeue top block in freelist
        int free = freeBlocks;
        byte[] data = SysLib.rawread(free, freeBlocks);
        short next = bytes2short(data, 0);
        freeBlocks = next;
        return free;
	}

	public boolean returnBlock( int oldBlockNumber ){
		// **TODO**
		// enqueue oldBlockNumber to top of freelist
        byte[] freeBlock = new byte[Disk.blockSize];
        int next = freeBlocks;
        SysLib.int2bytes(next, freeBlock, 0);
        SysLib.rawwrite(oldBlockNumber, freeBlock);
        freeBlocks = oldBlockNumber;
	}
}

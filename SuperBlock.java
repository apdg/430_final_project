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
	}

	void format(){
		// **TODO**
		// initialize Inodes, free blocks
	}
	
	void format( int numBlocks ){
		// **TODO**
		// initialize Inodes, free blocks
	}

	public int getFreeBlock(){
		// **TODO**
		// dequeue top block in freelist
	}

	public boolean returnBlock( int oldBlockNumber ){
		// **TODO**
		// enqueue oldBlockNumber to top of freelist
	}

}

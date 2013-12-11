// Inode.java

public class Inode {

	private final static int iNodeSize = 32;       // fix to 32 bytes
	private final static int directSize = 11;      // # direct pointers

	public int length;                             // file size in bytes
	public short count;                            // # file-table entries pointing to this
	public short flag;                             // 0 = unused, 1 = used, ...
	public short direct[] = new short[directSize]; // direct pointers
	public short indirect;                         // a indirect pointer

	Inode( ) {                                     // default constructor
		length = 0;
		count = 0;
		flag = 1;
		for ( int i = 0; i < directSize; i++ )
			direct[i] = -1;
		indirect = -1;
	}

	Inode ( short iNumber ) {
		int blkNumber = iNumber / 16 + 1;
		byte[] data = new byte[Disk.blockSize];
		SysLib.rawread( blkNumber, data );
		int offset = ( iNumber % 16 ) * iNodeSize;
		length = SysLib.bytes2int( data, offset );
		offset += 4;
		count = SysLib.bytes2short( data, offset );
		offset += 2;
		flag = SysLib.bytes2short( data, offset );
		offset += 2;
		for ( int i = 0; i < directSize; i++ ) {
			direct[i] = SysLib.bytes2short( data, offset );
			offset += 2;
		}
		indirect = SysLib.bytes2short( data, offset );
	}

	int toDisk( short iNumber ) {                  // save to disk as the i-th inode
	 	// **TODO**
	}

	int findIndexBlock(){
		if (length/Disk.blockSize < directSize && indirect > 0){
			
		}
		return -1;

	}

	boolean registerIndexBlock( short indexBlockNumber ){

	}

	int findTargetBlock( int offset ){
		int blockNumber = offset/Disk.blockSize;
		if (blockNumber < 0 ) 
			return -;
		else if ( blockNumber < directSize )
			return direct[blockNumber];
		else if ( blockNumber < 267 && indirect == 1 ) {
			// ok so I need to come back with one of the indirect blocks, how?

		}
		else
			return -1;

	}

	int registerTargetBlock( int offset, short targetBlockNumber ){

	}

	byte[] unregisterIndexBlock(){

	}

}

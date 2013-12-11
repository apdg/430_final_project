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
	 	
	 	// Read the corresponding block into byte buffer
	 	byte[] buffer = new byte[Disk.blockSize];
	 	// Calculate the offset within the buffer
	 	int blockNumber = iNumber / 16 + 1;
	 	SysLib.rawread(blockNumber, buffer);
	 	int offset = (iNumber % 16) * iNodeSize;
	 	
	 	SysLib.int2bytes(length, buffer, offset);
	 	offset += 4;
	 	SysLib.short2bytes(count, buffer, offset);
	 	offset += 2;
	 	SysLib.short2bytes(flag, buffer, offset);
	 	offset += 2;
	 	for (int i = 0; i < directSize; i++) {
	 		SysLib.short2bytes(direct[i], buffer, offset);
	 		offset += 2;
	 	}
	 	SysLib.short2bytes(indirect, buffer, offset);
	 	
	 	SysLib.rawwrite(blockNumber, buffer);
	 	return 0;
	}


	int findIndexBlock(){
		if (length/Disk.blockSize < directSize && indirect > 0){
			
		}
		return -1;

	}


	boolean registerIndexBlock( short indexBlockNumber ) {
		indirect = indexBlockNumber;
		byte[] buffer = new byte[Disk.blockSize];
		for (int i = 0; i < Disk.blockSize; i *= 2) {
			SysLib.short2bytes((short)-1, buffer, i);
		}
		SysLib.rawwrite(indirect, buffer);
		return true;
	}


	int findTargetBlock( int offset ) {
		int blockNumber = offset/Disk.blockSize;
		if (blockNumber < 0 ) 
			return -1;
		else if ( blockNumber < directSize )
			return direct[blockNumber];
		else if ( blockNumber < 267 && indirect == 1 ) {
			// ok so I need to come back with one of the indirect blocks, how?
			// Get the block of other pointers
			int block_index = (int)indirect;
			byte[] indirect_buf = new byte[Disk.blockSize];
			SysLib.rawread(block_index, indirect_buf);
			
			// Subtract 11 so we can get get the appropriate bytes within the
			// indirect block.
			block_index = blockNumber - directSize;
			block_index = (int)SysLib.bytes2short(indirect_buf, block_index * 2);
			return block_index;
		}
		else
			return -1;

	}


	int registerTargetBlock( int offset, short targetBlockNumber ){
		int blkNumber = offset / Disk.blockSize;
		if (blkNumber >= 11) {
			blkNumber -= 11;
			byte[] buffer = new byte[Disk.blockSize];
			if (indirect < 0 || indirect >= 1000) {
				System.out.printf("indirect: %d\n", indirect);
				return -1;
			}
			SysLib.rawread(indirect, buffer);
			
			int entry_offset = blkNumber * 2;
			SysLib.short2bytes(targetBlockNumber, buffer, entry_offset);
			SysLib.rawwrite(indirect, buffer);
		} else {
			direct[blkNumber] = targetBlockNumber;
		} 
		return 0;
	}


	byte[] unregisterIndexBlock(){
		byte[] buffer = new byte[Disk.blockSize];
		SysLib.rawread(indirect, buffer);
		return buffer;
	}

}

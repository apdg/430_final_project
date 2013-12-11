// FileSystem.java

public class FileSystem {

	private SuperBlock superblock;
	private Directory directory;
	private FileTable filetable;

	public FileSystem( int diskBlocks ) {
		superblock = new SuperBlock( diskBlocks );
		directory = new Directory( superblock.inodeBlocks );
		filetable = new FileTable( directory );
		FileTableEntry dirEnt = open( "/", "r" );
		int dirSize = fsize( dirEnt );
		if ( dirSize > 0 ) {
			byte[] dirData = new byte[dirSize];
			read( dirEnt, dirData );
			directory.bytes2directory( dirData );
		}
		close( dirEnt );
	}

	// SuperBlock.sync() writes the contents to Disk.  So this should as well.
	// SuperBlock(), Directory(SuperBlock), and FileTable(Directory) all read
	// from Disk, so sync() should not be a read operation.
	public void sync(){
		// **TODO**
		// Write each of the necessary blocks to Disk:
		superblock.sync();
		directory.sync();
		
			// SuperBlock
			// Directory Block
			// INode Blocks???
	}
	
	/**
	 * Formats the Disk (Disk.java's contents"
	 * @param int files the maximum number of files to create.
	 */
	public boolean format( int files ){
		// **TODO**
		// Do we just want to call sync() here?  I feel like they will be largely
		// the same.

		// SuperBlock.format()
		// filetable = new FileTable(directory);
		// Write Directory to Disk
		// Write Inode for file "/" to Disk.
		superblock.format( files );
		filetable = new FileTable( directory );
	}
	
	
	public FileTableEntry open( String filename, String mode ) {
		FileTableEntry fte = filetable.falloc(filename, mode);
		return fte;
	}


	// 
	public boolean close( FileTableEntry ftEnt ){
		// **TODO**
		// Close the ftEnt (not exactly sure what this would mean).
		// Write changes to Disk:
			// Inode
			// Directory
	}
	

	// Returns the file size of the corresponding file of the given FileTableEntry.
	public int fsize( FileTableEntry ftEnt ){
		return ftEnt.inode.length;
	}
	

	public int read( FileTableEntry ftEnt, byte[] buffer ) {
		// **TODO**
		// read in the appropriate 
		int offset = seekPtr;
		int block = ftEnt.inode.findTargetBlock(offset);
		byte[] block_buffer = new byte[Disk.blockSize];
		SysLib.rawread(block, block_buffer);
		
		int buffer_index = 0;
		
		while (offset < ftEnt.inode.length && buffer_index < buffer.length) {
			if (offset % Disk.blockSize == 0) {
				block = ftEnt.inode.findTargetBlock(offset);
				SysLib.rawread(block, block_buffer);
			}
			
			buffer[buffer_index] = block_buffer[offset];
			
			offset++;
			buffer_index++;
		}
	}
	

	public int write( FileTableEntry ftEnt, byte[] buffer ){
		// **TODO**
		// Read in the appropriate blocks
		// Write the contents of buffer to the file specified by ftEnt
		// 
	}
	

	public boolean delete( String filename ){
		// **TODO**
		// Remove the entry in Directory
		// Add all allocated blocks back to the freelist
		// 
	}
	

	int seek( FileTableEntry ftEnt, int offset, int whence ){
<<<<<<< HEAD
		if ( offset < 0 )
			offset = 0;
		if (whence = 0) 
			ftEnt.seekPtr = offset;
		else
			ftEnt.seekPtr += offset;
		if ( offset > ftEntry.inode.length )
			offset = ftEntry.inode.length;
		return 1;
	}
}
=======
		// **TODO**
		// udpate the seek pointer in ftEnt
		// Be sure to clamp to 0.
	}

}
<<<<<<< HEAD
>>>>>>> 3241a6aa8685575a6410ce5c0db8e1292e152ce2
=======
>>>>>>> 3241a6aa8685575a6410ce5c0db8e1292e152ce2

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
		// Do we just want to call sync() here?  I feel like they will be largely
		// the same.

		// SuperBlock.format()
		// Write Directory to Disk
		// Write Inode for file "/" to Disk.
		superblock.format( files );
		filetable = new FileTable( directory );
		// **TODO**
	}
	
	
	public FileTableEntry open( String filename, String mode ) {
		// Check if mode is valid: == "r" or "w" or "w+" or "a"
		FileTableEntry fte = filetable.falloc(filename, mode);
		return fte;

		/*
		FileTableEntry ftEnt = filetable.falloc( filename, mode );
		if ( mode == "w" ){			 // release all blocks belonging to this file
			if ( deallocAllBlocks( ftEnt ) == false )
				return null;
		}
		return ftEnt;*/
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
	

	public int read( FileTableEntry ftEnt, byte[] buffer ){
		// **TODO**
		// read in the appropriate 
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
		// **TODO**
		// udpate the seek pointer in ftEnt
		// Be sure to clamp to 0.
	}

}

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
		int dirSize = fsize( dirEnt ); // will be 0 when the Disk is first initialized.
		if ( dirSize > 0 ) {
			byte[] dirData = new byte[dirSize];
			read( dirEnt, dirData );
			directory.bytes2directory( dirData );
		} else {
			sync(); // this will write the directory to Disk and update the root inode.
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
		byte[] buffer = directory.directory2bytes();
		FileTableEntry dirEnt = open("/", "w");
		write(dirEnt, buffer);
		close(dirEnt);

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
		directory = new Directory( files );
		filetable = new FileTable( directory );
		sync();
		return true;
	}
	
	
	public FileTableEntry open( String filename, String mode ) {
		// Check if mode is valid: == "r" or "w" or "w+" or "a"
		if ( mode.equals("r") || mode.equals("w") || mode.equals("w+") || mode.equals("a")) {
			FileTableEntry fte = filetable.falloc(filename, mode);
			return fte;
		}
		else
			return null;

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
		if (ftEnt == null) {
			return false;
		}
		
		ftEnt.count--;
		if (ftEnt.count == 0) {
			ftEnt.inode.toDisk(ftEnt.iNumber);
			ftEnt.inode.count--;
			filetable.ffree(ftEnt);
		}
		return true;
	}
	

	// Returns the file size of the corresponding file of the given FileTableEntry.
	public int fsize( FileTableEntry ftEnt ){
		return ftEnt.inode.length;
	}
	

	public int read( FileTableEntry ftEnt, byte[] buffer ) {

		if (ftEnt == null) {
			return -1;
		}
		// **TODO**
		// read in the appropriate 
		int offset = ftEnt.seekPtr;
		int block = ftEnt.inode.findTargetBlock(offset);
		if (block >= 999 || block < 0) {
			System.out.println("ERROR!\n");
			return -1;
		}
		byte[] block_buffer = new byte[Disk.blockSize];
		SysLib.rawread(block, block_buffer);
		
		int buffer_index = 0;
		
		while (offset < ftEnt.inode.length && buffer_index < buffer.length) {
			if (offset % Disk.blockSize == 0) {  // Possible Source of OBOB!!!!
				block = ftEnt.inode.findTargetBlock(offset);
				SysLib.rawread(block, block_buffer);
				if (block >= 999 || block < 0) {
					System.out.println("ERROR!\n");
					return -1;
				}
			}
			
			buffer[buffer_index] = block_buffer[offset % Disk.blockSize];
			
			offset++;
			buffer_index++;
		}
		return buffer_index;
	}
	

	public int write( FileTableEntry ftEnt, byte[] buffer ){
		// **TODO**
		// Read in the appropriate blocks
		// Write the contents of buffer to the file specified by ftEnt
		// 
		// Check write permissions.
		if (ftEnt == null || ftEnt.mode.equals("r")) {
			return -1;
		}
		
		int offset = ftEnt.seekPtr;
		int block = -1;
		byte[] block_buffer = new byte[Disk.blockSize];
		System.out.printf("1...\n");
		
		// Want to read in the first block only if we actually have a file...
		if (ftEnt.inode.length > 0) {
			System.out.printf("2...\n");
			block = ftEnt.inode.findTargetBlock(offset);
			if (block < 0 || block >= 1000) {
				System.out.printf("block: %d\n", block);
				return -1;
			}
			SysLib.rawread(block, block_buffer);
		}
		
		System.out.printf("3...\n");
		int buffer_index = 0;
		
		while (buffer_index < buffer.length) {

			//System.out.printf("4...\n");
			if (offset % Disk.blockSize == 0) {
			
				System.out.printf("5...\n");
				if (offset > 0) {
				
					System.out.printf("6...\n");
					// Write the previous block back to Disk, but only if we had
					// a previous block.
					SysLib.rawwrite(block, block_buffer);
				}
				if (offset >= ftEnt.inode.length) {
				
					System.out.printf("7...\n");
					// grab a new block from freelist
					block = superblock.getFreeBlock();
					System.out.printf("8...\n");
					if (block < 0 || block >= 1000) {
						System.out.printf("block: %d\n", block);
						return -1;
					}
					ftEnt.inode.registerTargetBlock(offset, (short)block);
					SysLib.rawread(block, block_buffer);
				} else {
					block = ftEnt.inode.findTargetBlock(offset);
					if (block < 0 || block >= 1000) {
						System.out.printf("block: %d\n", block);
						return -1;
					}
					SysLib.rawread(block, block_buffer); 
				}
			}
			
			block_buffer[offset % Disk.blockSize] = buffer[buffer_index];
			
			offset++;
			buffer_index++;
		}
		ftEnt.inode.length = offset;
		ftEnt.inode.toDisk(ftEnt.iNumber);
		
		int bytes_written = buffer.length;
		return bytes_written; // or whatever success was....
		// Update inodes
	}
	

	public boolean delete( String filename ){
		// **TODO**
		// Remove the entry in Directory
		// Add all allocated blocks back to the freelist
		// 
		// Release Indirect Blocks
		// Release Indirect Index Block
		// Release Direct Blocks
		// Remove Directory Entry
		// Zero Inode
		// 
		int inumber = directory.namei(filename);
		Inode inode = directory.inodei(inumber);
		int length = inode.length;
		int num_blocks = length / Disk.blockSize;
		for (int i = 0; i < num_blocks && i < 11; i++) {
			superblock.returnBlock(inode.direct[i]);
		}
		
		if (num_blocks >= 11) {
			num_blocks -= 11;
			byte[] buffer = new byte[Disk.blockSize];
			SysLib.rawread((int)inode.indirect, buffer);
			for (int i = 0; i < 256; i++) {
				short block = SysLib.bytes2short(buffer, i * 2);
				if (block == -1) {
					break;
				} else {
					superblock.returnBlock((int)block);
				}
			}
			superblock.returnBlock((int)inode.indirect);
		}
		
		directory.ifree((short)inumber);
		
		return true;
	}
	

	int seek( FileTableEntry ftEnt, int offset, int whence ){
		if (ftEnt == null) {
			return -1;
		}

		if (whence == 0) {
			ftEnt.seekPtr = offset;
		} else {
			ftEnt.seekPtr += offset;
		}
		
		if ( offset > ftEnt.inode.length) {
			offset = ftEnt.inode.length;
		} else if (ftEnt.seekPtr < 0 ) {
			ftEnt.seekPtr = 0;
		}
		return 1;
	}
}

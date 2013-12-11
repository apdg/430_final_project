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
		FileTableEntry fte;
		if ( mode.equals("r") || mode.equals("w") || mode.equals("w+") || mode.equals("a")) {
			fte = filetable.falloc(filename, mode);
			//return fte;
		} else {
			fte = null;
		}	

		
		if ( mode == "w" ) {			 // release all blocks belonging to this file
			if ( deallocAllBlocks( fte ) == false ) {
				return null;
			}
		}
		return fte;
	}

	private boolean deallocAllBlocks(FileTableEntry fte) {
		if (fte == null) {
			return false;
		}
		
		int filesize = fte.inode.length;
		for (int i = 0; i < filesize; i += 512) {
			int targetblock = fte.inode.findTargetBlock(i);
			superblock.returnBlock(targetblock);
		}
		
		if (fte.inode.indirect != 0) {
			superblock.returnBlock(fte.inode.indirect);
			fte.inode.indirect = 0;
		}
		
		fte.inode.length = 0; 
		
		fte.inode.toDisk(fte.iNumber); // Write the iNode back to disk.
		
		return true;
	}

	// 
	public boolean close( FileTableEntry ftEnt ){

		if (ftEnt == null) {
			return false;
		}
		
		ftEnt.count--;
		if (ftEnt.count == 0) {

			ftEnt.inode.count--;
			// If the inode has been marked as deleted (3) and the last thread
			// to be accessing it has closed it, delete it instead. 
			if (ftEnt.inode.flag == 3 && ftEnt.inode.count == 0) {
				deallocAllBlocks(ftEnt);
				directory.ifree(ftEnt.iNumber);
			}
			filetable.ffree(ftEnt);
			ftEnt.inode.toDisk(ftEnt.iNumber);
		}
		return true;
	}
	

	// Returns the file size of the corresponding file of the given FileTableEntry.
	public int fsize( FileTableEntry ftEnt ){
		if (ftEnt == null) {
			return -1; // null FileTableEntry
		}
		
		return ftEnt.inode.length;
	}
	

	public int read( FileTableEntry ftEnt, byte[] buffer ) {

		if (ftEnt == null) {
			return -1;
		}
		
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

		
		// Check write permissions.
		if (ftEnt == null || ftEnt.mode.equals("r")) {
			return -1;
		}
		
		int offset = ftEnt.seekPtr;							// The current location in the file.
		int block = -1;										// Current block in file
		byte[] block_buffer = new byte[Disk.blockSize];		// Buffer to store block
		
		// Want to read in the first block only if we actually have a file.  That
		// is, if the inode says the file has a length greater than 0.
		if (ftEnt.inode.length > 0) {
			block = ftEnt.inode.findTargetBlock(offset);
			if (block < 0 || block >= 1000) {
				System.out.printf("block: %d\n", block);
				return -1;
			}
			SysLib.rawread(block, block_buffer);
		}
		
		// 
		int buffer_index = 0;
		
		// Loop over all elements in the given buffer.
		while (buffer_index < buffer.length) {


			// If the current offset is at the start of a block, we want to write
			// out the previous block (if there was one), and then read in the 
			// next block.  
			if (offset % Disk.blockSize == 0) {
			
				// If offset is > 0, then we are at the beginning of the 
				// second block or greater.
				if (offset > 0) {
				
					// Write the current block to disk before grabbing the next
					// one.  
					SysLib.rawwrite(block, block_buffer);
				}
				
				// If offset is >= the inodes length, then we have gone past the
				// end of the existing file.  In this case, we need to grab a 
				// new block from the free list.
				if (offset >= ftEnt.inode.length) {
				
					// grab a new block from freelist
					block = superblock.getFreeBlock();
					
					// Check if the free list returns a valid block.  If not, we
					// assume that there are no more free blocks.
					if (block < 0 || block >= 1000) {
						System.out.printf("block: %d\n", block);
						break;
					}
					
					// Read it in.
					ftEnt.inode.registerTargetBlock(offset, (short)block);
					SysLib.rawread(block, block_buffer);
				
				// If offset is < inode's length, then there should already be
				// blocks allocated.  In this case, we can just grab the next
				// block from the inode.
				} else {
					block = ftEnt.inode.findTargetBlock(offset);
					
					// Technically, this should never be out of bounds (if 
					// proper testing is done in the right places such as the 
					// above test).  Nevertheless, we'll be safe...
					if (block < 0 || block >= 1000) {
						System.out.printf("block: %d\n", block);
						break;
					}
					SysLib.rawread(block, block_buffer); 
				}
			}
			
			// Since we're not at the start of a block, or we've already handled
			// the new block, copy the buffer into the block buffer one element
			// at a time.
			block_buffer[offset % Disk.blockSize] = buffer[buffer_index];
			
			offset++;
			buffer_index++;
		}
		
		
		
		// We want to update the Inode length.  In the case where we opened the
		// file with "w+", the offset is not guaranteed to be past the end of the
		// original file.  
		if (offset >= ftEnt.inode.length) {
			ftEnt.inode.length = offset;
		}
		ftEnt.inode.toDisk(ftEnt.iNumber);
		
		int bytes_written = buffer_index;
		return bytes_written; 
	}
	

	// 
	public boolean delete( String filename ){


		int inumber = directory.namei(filename);
		
		// We don't want to delete the root file, and we don't want to try to 
		// delete a file that doesn't exist.
		if (inumber <= 0) {
			return false;
		}
		
		Inode inode = directory.inodei(inumber);
		
		if (inode.count > 0) {
			inode.flag = 3; // delete flag.
			// will be deleted when the current thread closes the file.
			return true;
		}
		
		
		// This section is identical to deallocAllBlocks();
		int filesize = inode.length;
		for (int i = 0; i < filesize; i += 512) {
			int targetblock = inode.findTargetBlock(i);
			superblock.returnBlock(targetblock);
		}
		
		if (inode.indirect != 0) {
			superblock.returnBlock(inode.indirect);
			inode.indirect = 0;
		}
		
		inode.length = 0; 
		
		directory.ifree((short)inumber);  // Remove the directory entry.
		
		inode.toDisk((short)inumber); // Write the iNode back to disk.
		
		
		return true;
		
	}
	

	int seek( FileTableEntry ftEnt, int offset, int whence ){
		if (ftEnt == null) {
			return -1; // Error null FileTableEntry
		}

		// Naively set the seek pointer to a relative location in the file based
		// on the whence value, the current seek value, and the offset.
		if (whence == 0) {
			ftEnt.seekPtr = offset;
		} else if (whence == 1) {
			ftEnt.seekPtr += offset;
		} else if (whence == 2) {
			ftEnt.seekPtr = ftEnt.inode.length + offset;
		} else {
			return -1; // invalid whence value.
		}
		
		// If the seekpointer is off the end of the file, clamp to the nearest
		// point within the file.
		if ( offset > ftEnt.inode.length) {
			offset = ftEnt.inode.length;
		} else if (ftEnt.seekPtr < 0 ) {
			ftEnt.seekPtr = 0;
		}
		
		return 0; // Returns 0 for success.
	}
}

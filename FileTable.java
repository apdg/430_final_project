// FileTable.java
import java.util.*;

public class FileTable {

	private Vector table;         // the actual entity of this file table
	private Directory dir;        // the root directory 

	public FileTable( Directory directory ) { // constructor
		table = new Vector( );     // instantiate a file (structure) table
		dir = directory;           // receive a reference to the Director
	}                             // from the file system

	// major public methods
	public synchronized FileTableEntry falloc( String filename, String mode ) {
		// allocate a new file (structure) table entry for this file name
		// allocate/retrieve and register the corresponding inode using dir
		// increment this inode's count
		// immediately write back this inode to the disk
		// return a reference to this file (structure) table entry
        FileTableEntry fte;
		int inode_index = dir.namei(filename);
		if (inode_index == -1) {
		
			if (mode.equals("r")) {
				return null;
			}
			
			inode_index = dir.ialloc(filename);
			//Inode inode = new Inode();
			//indoe.toDisk(inode_index);
			Inode inode = dir.inodei(inode_index);
			fte = new FileTableEntry(inode, (short)inode_index, mode);
			table.add(fte);
			
			return fte;
			
		} else {
			//Inode inode = new Inode(inode_index);
			Inode inode = dir.inodei(inode_index);
			fte = new FileTableEntry(inode, (short)inode_index, mode);
			table.add(fte);
			
			return fte;
		}
	}


	public synchronized boolean ffree( FileTableEntry fte ) {
		// receive a file table entry reference
		// save the corresponding inode to the disk
		// free this file table entry.
		// return true if this file table entry found in my table
		fte.inode.toDisk(fte.iNumber);
		boolean succeeded = table.remove(fte);
		return succeeded;

	}

	public synchronized boolean fempty( ) {
		return table.isEmpty( );  // return if table is empty 
	}                            // should be called before starting a format
}

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

	void sync(){
		// **TODO**
	}
	
	boolean format( int files ){
		// **TODO**
	}
	
	FileTableEntry open( String filename, String mode ){
		// **TODO**
	}
	
	FileTableEntry open( String filename, String mode ) {
		FileTableEntry ftEnt = filetable.falloc( filename, mode );
		if ( mode == "w" ){             // release all blocks belonging to this file
			if ( deallocAllBlocks( ftEnt ) == false )
				return null;
		}
		return ftEnt;
	}

	boolean close( FileTableEntry ftEnt ){
		// **TODO**
	}
	
	int fsize( FileTableEntry ftEnt ){
		// **TODO**
	}
	
	int read( FileTableEntry ftEnt, byte[] buffer ){
		// **TODO**
	}
	
	int write( FileTableEntry ftEnt, byte[] buffer ){
		// **TODO**
	}
	
	boolean delete( String filename ){
		// **TODO**
	}
	
	int seek( FileTableEntry ftEnt, int offset, int whence ){
		// **TODO**
	}

}
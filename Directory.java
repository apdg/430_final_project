// Directory.java

public class Directory {

	private static int maxChars = 30; // max characters of each file name

	// Directory entries
	private int fsize[];        // each element stores a different file size.
	private char fnames[][];    // each element stores a different file name.

	public Directory( int maxInumber ) { // directory constructor
		fsizes = new int[maxInumber];     // maxInumber = max files
		for ( int i = 0; i < maxInumber; i++ ) 
			fsize[i] = 0;                 // all file size initialized to 0
		fnames = new char[maxInumber][maxChars];
		String root = "/";                // entry(inode) 0 is "/"
		fsize[0] = root.length( );        // fsize[0] is the size of "/".
		root.getChars( 0, fsizes[0], fnames[0], 0 ); // fnames[0] includes "/"
	}

	// assumes data[] received directory information from disk
	// initializes the Directory instance with this data[]
	public int bytes2directory( byte data[] ) {
		int offset = 0;
		for ( int i = 0; i < fsizes.length; i++, offset += 4 ) {
			fsizes[i] = SysLib.bytes2int( data, offset );
        } 
        for ( int i = 0; i < fnames.length; i++, offset += maxChars * 2 ) {
			String fname = new String( data, offset, maxChars * 2 );
			fname.getChars( 0, fsizes[i], fnames[i], 0 );
		}

	}

    // Converts the Directory information into a plain byte array and returns it.
    // This byte array will then be written back to disk.
	public byte[] directory2bytes( ) {
        int byte_array_size = fsize.length * 4 +
                              fnames.length * maxChars * 2;
        byte data[byte_array_size];
        int offset = 0;
        // add fsize
        for (int i = 0; i < fsizes.length; i++, offset += 4) {
            SysLib.int2bytes(fsizes[i], data, offset);
        }
        // add fnames
        for (int i = 0; i < fnames.length, i++, offset += maxChars * 2) {
            for (int j = 0; j < fsizes[i]; j++) {
                short2bytes((short)fnames[i][j], data, offset + j);
            }
        }
        return data;
	}

    
	public short ialloc( String filename ) {
	// filename is the one of a file to be created.
	// allocates a new inode number for this filename
        short iNumber = 0;
        for (int i = 0; i < fsizes.length; i++) {
            if (fsizes[i] == 0) {
                iNumber = i;
                break;
            }
        }

        fsizes[iNumber] = filename.size();
        filename.getChars(0, fsizes[iNumber], fnames[iNumber], 0);

        return num_inodes;
	}

	public boolean ifree( short iNumber ) {
	// deallocates this inumber (inode number)
	// the corresponding file will be deleted.
        fsizes[iNumber] = 0;
        return true;
	}

	public short namei( String filename ) {
	// returns the inumber corresponding to this filename
	    short iNumber;
        for (int i = 0; i < fnames.length; i++, iNumber++) {
            String compname = new String(fnames[i], 0, fsizes[i]);
            if (compname.compareTo(filename) == 0) {
                return iNumber;
            }
        }
    }

}

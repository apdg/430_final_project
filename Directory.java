// Directory.java

public class Directory {

	private static int maxChars = 30; // max characters of each file name

	// Directory entries
	private int fsize[];        // each element stores a different file size.
	private char fnames[][];    // each element stores a different file name.
	
	private Inode inodes[];		// 

	public Directory( int maxInumber ) { // directory constructor
		fsize = new int[maxInumber];     // maxInumber = max files
		inodes = new Inode[maxInumber];
		for ( int i = 0; i < maxInumber; i++ ) 
			fsize[i] = 0;                 // all file size initialized to 0
		fnames = new char[maxInumber][maxChars];
		String root = "/";                // entry(inode) 0 is "/"
		fsize[0] = root.length( );        // fsize[0] is the size of "/".
		root.getChars( 0, fsize[0], fnames[0], 0 ); // fnames[0] includes "/"
	}

	// assumes data[] received directory information from disk
	// initializes the Directory instance with this data[]
	public int bytes2directory( byte data[] ) {
		int offset = 0;
		for ( int i = 0; i < fsize.length; i++, offset += 4 ) {
			fsize[i] = SysLib.bytes2int( data, offset );
        } 
        for ( int i = 0; i < fnames.length; i++, offset += maxChars * 2 ) {
			String fname = new String( data, offset, maxChars * 2 );
			fname.getChars( 0, fsize[i], fnames[i], 0 );
		}
        return 0;
	}

    // Converts the Directory information into a plain byte array and returns it.
    // This byte array will then be written back to disk.
	public byte[] directory2bytes( ) {
        int byte_array_size = fsize.length * 4 +
                              fnames.length * maxChars * 2;
        byte data[] = new byte[byte_array_size];
        int offset = 0;
        // add fsize
        for (int i = 0; i < fsize.length; i++, offset += 4) {
            SysLib.int2bytes(fsize[i], data, offset);
        }
        // add fnames
        for (int i = 0; i < fnames.length; i++, offset += maxChars * 2) {
            for (int j = 0; j < fsize[i]; j++) {
                SysLib.short2bytes((short)fnames[i][j], data, offset + j*2);
            }
        }
        return data;
	}

   


	// filename is the one of a file to be created.
	// allocates a new inode number for this filename
	public short ialloc( String filename ) {
        short iNumber = 0;
        for (int i = 0; i < fsize.length; i++) {
            if (fsize[i] == 0) {
                iNumber = (short)i;
                break;
            }
        }

        fsize[iNumber] = filename.length();

        filename.getChars(0, fsize[iNumber], fnames[iNumber], 0);
        
        return iNumber;
	}



	// deallocates this inumber (inode number)
	// the corresponding file will be deleted.
	public boolean ifree( short iNumber ) {
        for (int i = 0; i < fsize[iNumber]; i++){
            fnames[iNumber][i] = (char)0;
        }
        fsize[iNumber] = 0;
        return true;
	}



	// returns the inumber corresponding to this filename
	public short namei( String filename ) {
	    short iNumber = -1;
        for (int i = 0; i < fnames.length; i++, iNumber++) {
            String compname = new String(fnames[i], 0, fsize[i]);
            if (compname.compareTo(filename) == 0) {
                iNumber = (short)i;
                break;
            }
        }
        return iNumber;
    }
    
    // Returns a reference to an in-memory Inode at the given index.
    public Inode inodei( int iNumber ) {
    	if (inodes[iNumber] == null) {
    		inodes[iNumber] = new Inode((short)iNumber);
    	}
    	return inodes[iNumber];
    }


    /* FOR TESTING PURPOSES ONLY
    public void short2bytes(short s, byte[] b, int offset) {
        b[offset] = (byte)(s >> 8);
        b[offset + 1] = (byte)s;
    }

    public short bytes2short(byte[] b, int offset) {
        short s = 0;
        s += b[offset] & 0xff;
        s <<= 8;
        s += b[offset + 1] & 0xff;
        return s;
    }

    public void int2bytes(int i, byte[] b, int offset) {
        b[offset] = (byte)(i >> 24);
        b[offset + 1] = (byte)(i >> 16);
        b[offset + 2] = (byte)(i >> 8);
        b[offset + 3] = (byte)i;
    }

    public int bytes2int(byte[] b, int offset) {
        int n = ((b[offset] & 0xff) << 24) + ((b[offset+1] & 0xff) << 16) +
                ((b[offset+2] & 0xff) << 8) + (b[offset+3] & 0xff);
        return n;
    }
    */
}

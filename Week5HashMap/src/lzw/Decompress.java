package lzw;

import java.io.*;

public class Decompress {

    final static int MAX_CODES = 4096;
    final static int BYTE_SIZE = 8;
    final static int EXCESS = 4;
    final static int ALPHA = 256;
    final static int MASK = 15;
    static int[] s;
    static int size;
    static Element[] h;
    static int leftOver;
    static boolean bitsLeftOver;
    static BufferedInputStream in;
    static BufferedOutputStream out;

    private static void setFiles(String bestand) throws IOException {
        String inputFile, outputFile;
        
            in = new BufferedInputStream(new FileInputStream(bestand));
            outputFile = bestand.substring(0, bestand.length() - 4);
            out = new BufferedOutputStream(new FileOutputStream(bestand+".decr"));
        
    }

    private static void output(int code) throws IOException {
        size = -1;
        while (code >= ALPHA) {
            s[++size] = h[code].suffix;
            code = h[code].prefix;
        }
        s[++size] = code;
        for (int i = size; i >= 0; i--) {
            out.write(s[i]);
        }
    }

    private static int getCode() throws IOException {
        int c = in.read();
        if (c == -1) {
            return -1;
        }

        int code;
        if (bitsLeftOver) {
            code = (leftOver << BYTE_SIZE) + c;
        } else {
            int d = in.read();
            code = (c << EXCESS) + (d >> EXCESS);
            leftOver = d & MASK;
        }
        bitsLeftOver = !bitsLeftOver;
        return code;
    }

    private static void decompress() throws IOException {
        int codeUsed = ALPHA;
        s = new int[MAX_CODES];
        h = new Element[MAX_CODES];

        int pcode = getCode(), ccode;
        if (pcode >= 0) {
            s[0] = pcode;
            out.write(s[0]);
            size = 0;

            do {
                ccode = getCode();
                if (ccode < 0) {
                    break;
                }
                if (ccode < codeUsed) {
                    output(ccode);
                    if (codeUsed < MAX_CODES) {
                        h[codeUsed++] = new Element(pcode, s[size]);
                    }
                } else {
                    h[codeUsed++] = new Element(pcode, s[size]);
                    output(ccode);
                }
                pcode = ccode;
            } while (true);
        }
        out.close();
        in.close();
    }

    public static void main(String[] args) throws IOException {
        setFiles("/home/martijn/Dropbox/han/courses/jaar2/owe6/informatica/code/HashMap/src/lzw/file.txt.zzz");
        decompress();
    }
}

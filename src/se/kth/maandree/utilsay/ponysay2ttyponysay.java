/**
 * ponysay2ttyponysay — TTY suitifying ponysay pony tool
 *
 * Copyright © 2012  Mattias Andrée (maandree@kth.se)
 *
 * This prorgram is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.kth.maandree.utilsay;

import java.util.*;
import java.io.*;


/**
 * The main class of the ponysay2ttyponysay program
 *
 * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
 */
public class ponysay2ttyponysay
{
    /**
     * Non-constructor
     */
    private ponysay2ttyponysay()
    {
	assert false : "This class [ponysay2ttyponysay] is not meant to be instansiated.";
    }
    
    
    
    /**
     * This is the main entry point of the program
     * 
     * @param  args  Startup arguments, start the program with </code>--help</code> for details
     * 
     * @throws  IOException  On I/O exception
     */
    public static void main(final String... args) throws IOException
    {
	if ((args.length > 0) && args[0].equals("--help"))
	{
	    System.out.println("TTY suitifying ponysay pony tool");
	    System.out.println();
	    System.out.println("USAGE:  ponysay2ttyponysay [SOURCE... | < SOURCE > TARGET]");
	    System.out.println();
	    System.out.println("Source (STDIN):  Regular unisay pony");
	    System.out.println("Target (STDOUT): New TTY unisay pony");
	    System.out.println();
	    System.out.println();
	    System.out.println("Copyright (C) 2012  Mattias Andrée <maandree@kth.se>");
	    System.out.println();
	    System.out.println("This program is free software: you can redistribute it and/or modify");
	    System.out.println("it under the terms of the GNU General Public License as published by");
	    System.out.println("the Free Software Foundation, either version 3 of the License, or");
	    System.out.println("(at your option) any later version.");
	    System.out.println();
	    System.out.println("This program is distributed in the hope that it will be useful,");
	    System.out.println("but WITHOUT ANY WARRANTY; without even the implied warranty of");
	    System.out.println("MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the");
	    System.out.println("GNU General Public License for more details.");
	    System.out.println();
	    System.out.println("You should have received a copy of the GNU General Public License");
	    System.out.println("along with this program.  If not, see <http://www.gnu.org/licenses/>.");
	    System.out.println();
	    System.out.println();
	    return;
	}
	
	if (args.length == 0)
            convert(stdin, stdout);
        else
            for (final String arg : args)
		{
		    String file = (new File(arg)).getAbsolutePath();
		    String outfile = file.substring(0, file.lastIndexOf("/"));
		    outfile = outfile.substring(0, outfile.lastIndexOf("/"));
		    outfile += "/ttyponies";
		    outfile += file.substring(file.lastIndexOf("/"));
		    
		    InputStream fin = new BufferedInputStream(new FileInputStream(new File(arg)));
		    PrintStream fout = new PrintStream(new BufferedOutputStream(new FileOutputStream(new File(outfile))));
		    
		    convert(fin, fout);
		    
		    fin.close();
		    fout.close();
		}
    }
    
    
    private static final InputStream stdin = System.in;
    private static final PrintStream stdout = System.out;

    
    private static void convert(final InputStream in, final PrintStream out) throws IOException
    {
	int[] metabuf = {-1, -1, -1, -1, -1};
	boolean dollar = false, metadata = true;
	
	for (int d; (d = in.read()) != -1;)
	{
	    metabuf[0] = metabuf[1]; metabuf[1] = metabuf[2]; metabuf[2] = metabuf[3]; metabuf[3] = metabuf[4]; metabuf[4] = d;
	    if (metadata)
	    {
		out.write(d);
		if ((metabuf[0] == '\n') && (metabuf[1] == '$') && (metabuf[2] == '$') && (metabuf[3] == '$') && (metabuf[4] == '\n'))
		    metadata = dollar = false;
		continue;
	    }
	    else if ((metabuf[0] == -1) && (metabuf[1] == '$') && (metabuf[2] == '$') && (metabuf[3] == '$') && (metabuf[4] == '\n'))
	    {
		metadata = true;
		out.write(d);
		continue;
	    }
	    
	    if (d == '$')
	    {
		dollar ^= true;
		out.write(d);
	    }
	    else if (dollar)
		out.write(d);
	    else if (d == '\033')
	    {
                d = in.read();
                if (d == '[')
		{
		    d = in.read();
		    if (d == 'm')
			out.print("\033]P7aaaaaa\033]Pfffffff\033[0m");
		    
		    int lastlast = 0;
		    int last = 0;
		    int item = 0;
		    for (;;)
		    {
			if ((d == ';') || (d == 'm'))
			{
			    item = -item;
			    
			    if      (item == 0)   out.print("\033]P7aaaaaa\033]Pfffffff\033[0m");
			    else if (item == 39)  out.print("\033[39m");
			    else if (item == 49)  out.print("\033[49m");
			    else if ((last == 5) && (lastlast == 38))
			    {
				Colour colour = new Colour(item);
				out.print(getOSIPCode(colour.red, colour.green, colour.blue, false));
			    }
			    else if ((last == 5) && (lastlast == 48))
			    {
				Colour colour = new Colour(item);
				out.print(getOSIPCode(colour.red, colour.green, colour.blue, true));
			    }
			    else if ((item != 5) || ((last != 38) && (last != 48)))
				if ((item != 38) && (item != 48))
				{
				    System.err.println("Not a pretty pony.  Stop.");
				    System.exit(-1);
				}
			    
			    lastlast = last;
			    last = item;
			    item = 0;
			    if (d == 'm')
				break;
			}
			else
			    item = (item * 10) - (d & 15);
			d = in.read();
		    }
		}
                else
		{
		    System.err.println("Not a pretty pony.  Stop.");
		    System.exit(-1);
		}
	    }
	    else
		out.write(d);
	}
    }
    
    
    private static final String HEX = "0123456789abcdef";
    private static String getOSIPCode(final int r, final int g, final int b, final boolean background)
    {
        String code = new String();
        code += HEX.charAt((r >> 4) & 15);
        code += HEX.charAt( r       & 15);
        code += HEX.charAt((g >> 4) & 15);
        code += HEX.charAt( g       & 15);
        code += HEX.charAt((b >> 4) & 15);
        code += HEX.charAt( b       & 15);
	
        return "\033]P" + (background ? '7' : 'f') + code + "\033[" + (background ? "4" : "1;3") + "7m";
    }

}

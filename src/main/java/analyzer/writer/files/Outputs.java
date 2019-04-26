package analyzer.writer.files;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Outputs
{

    static Path base = Paths.get( "output" );

    private static Path mkdirs( Path path )
    {
        path.toFile().mkdirs();
        return path;
    }

    private static File get( Path dir, String name )
    {
        return mkdirs( dir ).resolve( name ).toFile();
    }

    public static OutDir base()
    {
        return new OutDir( base );
    }

    public static class OutDir
    {
        private final Path dir;

        OutDir( Path dir )
        {
            this.dir = dir;
        }

        public OutDir dir( String name )
        {
            return new OutDir( dir.resolve( name ) );
        }

        public BufferedWriter file( String name ) throws IOException
        {
            return new BufferedWriter( new FileWriter( get( dir, name ), true ) );
        }
    }
}

package analyzer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LogFileCollector
{

    public static List<String> getAllFilesInFolder( String folderLocation ){
        File folder = new File( folderLocation );
        File[] listOfFiles = folder.listFiles();
        List<String> fileNames = new ArrayList<>();

        for ( File listOfFile : listOfFiles )
            if ( listOfFile.isFile() )
            {
                if ( listOfFile.getName().contains( "query.log" ) && !listOfFile.getName().endsWith( ".zip" ))
                    fileNames.add( listOfFile.getAbsolutePath() );
            }else{
                fileNames.addAll( getAllFilesInFolder( listOfFile.getAbsolutePath() ) );
            }


        return fileNames;
    }
}

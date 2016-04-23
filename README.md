# JSONRetro
This is a tool that translates Retrosheet play-by-play CSV files into JSON


If you **do not hava Java installed**:

1. https://java.com/en/download/manual.jsp
 

If you have Java installed and use Windows:

1. [Download Retrosheet event files](http://www.retrosheet.org/game.htm)

2. Extract the archives into JSONRetro/data

3. Open command prompt (Windows+R "cmd"+Enter)

4. navigate to JSONRetro/bin

5. java -jar JSONRetro.jar

That will generate the .json files into JSONRetro/bin/out



NOTES:

This generator ignores all lines in a Retrosheet file starting with com,ladj,badj,padj and version.

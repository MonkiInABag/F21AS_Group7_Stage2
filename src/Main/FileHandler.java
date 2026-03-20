package Main;

import java.io.*;
import java.util.*;

import Exception.FileReadException;
import Exception.FileWriteException;

public class FileHandler {
    
    private List<String> rows; //hold the lines as an array
    private List<List<String>> fileArray; //hold the the file as an array of rows (2D array)
    private File file; 
    private Scanner reader;
    private int numCollumns; //number of collumns in file
    private FileWriter writer;
    private String fileName;
    private String headerRow; //store the header row

    public FileHandler(String fileName) throws FileReadException {
        this.rows = new ArrayList<>();
        this.fileArray = new ArrayList<>();
        this.numCollumns = -1;
        this.fileName = fileName;

        try{
            this.file = new File(this.fileName);
            this.reader = new Scanner(this.file);
            this.writer = new FileWriter(this.fileName, true); //needs the true to append instead of truncate

        }catch(IOException e){
            throw new FileReadException("Error opening file '" + fileName + "': " + e.getMessage());
        }
    }
    
    public void readFile() throws FileReadException {
        //try to create a new BufferedReader object to read the file. 
        //Will fail and catch if file cannot be read, eg if it isnt found in directory.
        String line;
        boolean isFirstLine = true;

        while (this.reader.hasNextLine()) {
            line = this.reader.nextLine();
            //assume file is always a correctly formatted csv
            String[] parts = line.split(",");

            // Dont read header row
            if (isFirstLine) {
                this.numCollumns = parts.length; // use the head row to determine number of collumns
                this.headerRow = line; //store the header row
                isFirstLine = false;
                continue;
            }

            //check if row has correct number of collumns, if not skip it
            if (parts.length != numCollumns) {
                continue;
            }else{
                // add parts of line to rows list. Then add rows to fileArray
                List<String> row = new ArrayList<>();
                for (String part : parts) {
                    part = part.trim(); // remove leading and trailing whitespace
                    row.add(part); 
                }
                this.fileArray.add(row);
            }
        }        
    }

    public int getNumCollumns()  throws FileReadException  {
        //if there is no collums, file is empty
        if (this.numCollumns == 0) {
            throw new FileReadException("File seems to be empty.");
            // if numCollumns is null, file has not been read yet. 
        } else if (this.numCollumns == -1) {//assuming null doesnt expand to 0 as an int. 
            throw new FileReadException("File has not been read yet.");
        }else{
            return this.numCollumns;
        }
    }

    public int getNumRows() throws FileReadException  {
        //if fileArray is null, file has not been read yet. 
        if (this.fileArray == null) {
            throw new FileReadException("File has not been read yet.");
        }else{
            return this.fileArray.size();
        }
    }

    //return the 2D list
    public List<List<String>> getFileArray() throws FileReadException  {
        //check if file has been read yet, if not throw exception.
        if (this.fileArray == null) {
            throw new FileReadException("File has not been read yet.");
        }else{
            return this.fileArray;
        }
    }

    public List<String> getRow(int rowIndex)  throws FileReadException {
        //check if file has been read yet, if not throw exception.
        if (this.fileArray == null) {
            throw new FileReadException("File has not been read yet.");
        }else if(rowIndex <0 || rowIndex >= getNumRows()){
            throw new FileReadException("Row index out of bounds.");
        }else{
            return this.fileArray.get(rowIndex);
        }
    }

    public void appendRow(List<String> row) throws FileReadException, FileWriteException {
        //check if file has been read yet, if not throw exception.
        if (this.fileArray == null) {
            throw new FileReadException("File has not been read yet.");
        }else if(row.size() != getNumCollumns()){
            throw new FileReadException("Row does not have correct number of collumns.");
        }else{
            this.fileArray.add(row);
            try{
                this.writer.write("\n"+String.join(",", row)); //add commas between items, add new line at end
                this.writer.flush(); //flush the writer to ensure data is written to file
                this.writer.close(); //close the writer after writing
                this.writer = new FileWriter(this.fileName, true); //reopen in append mode for next write
            }catch(IOException e){
                throw new FileWriteException("Error writing new row to file '" + fileName + "': " + e.getMessage());
            }
        }
    }

    //TODO: removing row shouldnt have to rewrite the whole file, brings dequeue to O(n) when they could be O(1).
    public void removeRow(int rowIndex) throws FileReadException, FileWriteException {
        //check if file has been read yet, if not throw exception.
        if (this.fileArray == null) {
            throw new FileReadException("File has not been read yet.");
        }else if(rowIndex <0 || rowIndex >= getNumRows()){
            throw new FileReadException("Row index out of bounds.");
        }else{
            this.fileArray.remove(rowIndex);
            try{
                //rewrite the entire file with the updated fileArray
                this.writer = new FileWriter(this.fileName); //recreate writer to overwrite file
                this.writer.write(this.headerRow); //write the header row first
                for (List<String> row : this.fileArray) {
                    this.writer.write("\n"+String.join(",", row));
                }
                this.writer.flush(); //flush the writer to ensure data is written to file
                this.writer.close(); //close the writer to ensure file is released
            }catch(IOException e){
                throw new FileWriteException("Error rewriting file '" + fileName + "' after removing row: " + e.getMessage());
            }
        }
    }
}
package yago;

import java.io.BufferedReader;

import java.io.BufferedWriter;

import java.io.FileNotFoundException;

import java.io.FileReader;

import java.io.FileWriter;

import java.io.IOException;


 


 

public class test {

      public static void main(String[] args) {

            try {

            // read file content from file
           

            FileReader reader = new FileReader("H:/myself/yago/yagoFacts.ttl");

            BufferedReader br = new BufferedReader(reader);

           

            String str = null;

           

            while((str = br.readLine()) != null) {
                  System.out.println(str);
            }

           

            br.close();

            reader.close();

      }

      catch(FileNotFoundException e) {

                  e.printStackTrace();

            }

            catch(IOException e) {

                  e.printStackTrace();

            }

      }


 

}
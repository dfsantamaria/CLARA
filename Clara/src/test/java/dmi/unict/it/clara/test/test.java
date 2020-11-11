/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dmi.unict.it.clara.test;

import dmi.unict.it.clara.core.Clara;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.Order;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

/**
 *
 * @author Daniele Santamaria
 */
public class test
{
    static Clara ontocore;
    
          
    @Test
    @Order(1)
    public void testAddRemoveManualUser()
    {
        ByteArrayInputStream request;          
          
          //ADDING USERS
          String id= "";        
          String userId="";          
          InputStream userData=readData("ontologies/test/alan.owl"); 
          userId=ontocore.addUser(userData);
          try 
          {
              ontocore.removePermanentUser(userId);
          } 
          catch (Exception ex) 
          {
              System.out.println("Error deleting user");
          } 
    }
    
    @Test
    @Order(2)
    public void testParseAddUserRequest()
    {
      //   ADDING USERS
          ontocore.parseRequest(readData("ontologies/test/alan.owl"));
          ByteArrayInputStream request=readData("ontologies/test/add-user-request.owl");  
          String out=toStringOntology(ontocore.parseRequest(request))[0];          
          writeDown("addUserRequest", out);
    }
    
   
    
    @Test
    @Order(3)
    public void testParseUserRequest()
    {
          //Manually adding a device  
        ontocore.parseRequest(readData("ontologies/test/lightagent-from-template.owl"));    
        ontocore.parseRequest(readData("ontologies/test/rasb-lightagent.owl")); 
        System.out.println("Adding light agent");
        ontocore.addDevice("http://www.dmi.unict.it/lightagent.owl");  
        
         //ADDING USERS                
          String userId="";          
          InputStream userData=readData("ontologies/test/alan.owl"); 
          userId=ontocore.addUser(userData);
          
          //add config
                            
          String confId="";          
          userData=readData("ontologies/test/alan-config.owl"); 
          confId=ontocore.addDeviceConfiguration(userData);
                      
         //The request
        ByteArrayInputStream request=readData("ontologies/test/user-request.owl");  
        String out=toStringOntology(ontocore.parseRequest(request))[0];
        writeDown("userRequest", out);
        
       //REMOVE
       
       
        try 
          {               
              ontocore.removePermanentConfigurationFromDevice(confId);
              ontocore.removePermanentUser(userId);
              ontocore.removePermanentDevice("http://www.dmi.unict.it/lightagent.owl");
              ontocore.refreshDataRequest();
          } 
          catch (Exception ex) 
          {
              System.out.println("Error deleting user");
          } 
        
    }
    
    @Test
    @Order(4)
    public void testParseInstallDeviceRequest()
    {      
          ByteArrayInputStream request=readData("ontologies/test/install-request-test.owl");  
          String out=toStringOntology(ontocore.parseRequest(request))[0];
          writeDown("installDeviceRequest", out);       
    }
    
    @Test
    @Order(5)
    public void testParseUninstallDeviceRequest()
    {      
        //Manually adding a device  
        ontocore.parseRequest(readData("ontologies/test/lightagent-from-template.owl"));    
        ontocore.parseRequest(readData("ontologies/test/rasb-lightagent.owl")); 
        System.out.println("Adding light agent");
        ontocore.addDevice("http://www.dmi.unict.it/lightagent.owl");
        ByteArrayInputStream request=readData("ontologies/test/uninstall-request-test.owl");  
        String out=toStringOntology(ontocore.parseRequest(request))[0];
        writeDown("uninstallDeviceRequest", out);    
        //REMOVE
        ontocore.removePermanentDevice("http://www.dmi.unict.it/lightagent.owl");
    }
    
    @Test
    @Order(6)
    public void testParseCheckstallDeviceRequest()
    {      
        //Manually adding a device  
        ontocore.parseRequest(readData("ontologies/test/lightagent-from-template.owl"));    
        ontocore.parseRequest(readData("ontologies/test/rasb-lightagent.owl")); 
        System.out.println("Adding light agent");
        ontocore.addDevice("http://www.dmi.unict.it/lightagent.owl");
        ByteArrayInputStream request=readData("ontologies/test/light-check-install-request.owl");  
        String out=toStringOntology(ontocore.parseRequest(request))[0];
        writeDown("checkinstallDeviceRequest", out);    
        //REMOVE
        ontocore.removePermanentDevice("http://www.dmi.unict.it/lightagent.owl");
    }
    
    
    @Test
    @Order(7)
    public void testParseAddUserConfigRequest()
    {
         ByteArrayInputStream request;          
          //ADDING USERS
          String id= "";        
          String userId="";          
          InputStream userData=readData("ontologies/test/alan.owl"); 
          userId=ontocore.addUser(userData);
          request=readData("ontologies/test/add-user-configuration-request.owl");  
          String out=toStringOntology(ontocore.parseRequest(request))[0];
          writeDown("userConfigRequest", out);
          
          try 
          {
              ontocore.removePermanentUser(userId);
          } 
          catch (Exception ex) 
          {
              System.out.println("Error deleting user");
          }      
    }
    
    
    @Test
    @Order(8)
    public void testParseRemoveUserRequest()
    {
          ByteArrayInputStream request;   
          
          //ADDING USERS
          String id= "";        
          String userId="";          
          InputStream userData=readData("ontologies/test/alan.owl"); 
          userId=ontocore.addUser(userData);
          request=readData("ontologies/test/remove-user-request.owl");  
          String out=toStringOntology(ontocore.parseRequest(request))[0];
          writeDown("removeUserRequest", out);          
          try 
          {
              ontocore.removePermanentUser(userId);
          } 
          catch (Exception ex) 
          {
              System.out.println("Error deleting user");
          }       
    }
    
    @BeforeClass
    public static void setUp()
    {      
        new File("ontologies"+File.separator+"outTest").delete();      
        File ontoFile=new File("ontologies/main/oasis.owl");
        File aboxFile=new File("ontologies/main/oasis-abox.owl");
       // File dataFile=new File("ontologies/main/dataset.owl");
        ontocore=new Clara();
        try
          { 
            FileUtils.cleanDirectory( (Paths.get("ontologies"+File.separator+"devices")).toFile());
            ontocore.setOntologiesDeviceConfigurationsPath(Paths.get("ontologies"+File.separator+"devConfigs"));
            ontocore.setOntologiesDevicesPath(Paths.get("ontologies"+File.separator+"devices"));
            ontocore.setMainOntologiesPath(Paths.get("ontologies"+File.separator+"main"));
            ontocore.setOntologiesUsersPath(Paths.get("ontologies"+File.separator+"users"));
            ontocore.setQueryPath(Paths.get("ontologies"+File.separator+"queries"));
            ontocore.setSatellitePath(Paths.get("ontologies"+File.separator+"satellite"));
            ontocore.setBackupPath(Paths.get("ontologies"+File.separator+"backup"));
            
            ontocore.setMainOntology(ontoFile); 
            ontocore.setMainAbox(aboxFile);
            
            ontocore.setDataBehaviorOntology("http://www.dmi.unict.it/prof-onto-behavior.owl","behavior.owl");  
            ontocore.setDataRequestOntology("http://www.dmi.unict.it/prof-onto-request.owl","request.owl");
            ontocore.setDataBeliefOntology("http://www.dmi.unict.it/prof-onto-belief.owl","belief.owl");
            ontocore.loadDevicesFromPath(false); //use this if the devices folder is not empty 
           
            
           // ontocore.startReasoner();
          } 
        catch (OWLOntologyCreationException | OWLOntologyStorageException | IOException ex)
          {
            Logger.getLogger(test.class.getName()).log(Level.SEVERE, null, ex);
          }
        
          if(!(new File("ontologies/devices/homeassistant.owl").exists()))
          {
            InputStream assistantData=readData("ontologies/test/homeassistant.owl");            
            String dv= ontocore.addDevice(assistantData);
            System.out.println("Connection of Assistant:");
            for( String s : ontocore.getConnectionInfo(dv))
                System.out.println(s);
          }     
        
    }
    
    
    @Test
    @Order(9)
    public void testParseAddDataBeliefRequest()
    {
          ByteArrayInputStream request;         
          request=readData("ontologies/test/add-belief-request.owl");  
          String out=toStringOntology(ontocore.parseRequest(request))[0];
          writeDown("userAddBeliefRequest", out);             
    }
    
   
    @Test
    @Order(10)
    public void testDirectParseInstallDeviceRequest()
    {      
          ByteArrayInputStream request=readData("ontologies/test/direct-install-request-test.owl");  
          String out=toStringOntology(ontocore.parseRequest(request))[0];
          writeDown("directInstallDeviceRequest", out);       
    }
    
    
    @Test
    @Order(11)
    public void testParseUpdateDeviceRequest()
    {      
        //Manually adding a device  
        ontocore.parseRequest(readData("ontologies/test/lightagent-from-template.owl"));    
        ontocore.parseRequest(readData("ontologies/test/rasb-lightagent.owl")); 
        System.out.println("Adding light agent");
        ontocore.addDevice("http://www.dmi.unict.it/lightagent.owl");
        ByteArrayInputStream request=readData("ontologies/test/light-update-request.owl");  
        String out=toStringOntology(ontocore.parseRequest(request))[0];
        writeDown("updateDeviceRequest", out);    
        //REMOVE
        ontocore.removePermanentDevice("http://www.dmi.unict.it/lightagent.owl");
    }
    
    @Test
    @Order(12)
    public void testUpdateDeviceRequest() throws OWLOntologyStorageException
    {      
        //Manually adding a device  
        ontocore.parseRequest(readData("ontologies/test/lightagent-from-template.owl"));    
        ontocore.parseRequest(readData("ontologies/test/rasb-lightagent.owl")); 
        System.out.println("Adding light agent");
        ontocore.addDevice("http://www.dmi.unict.it/lightagent.owl");
        
        //ADDING USERS                
        String userId="";          
        InputStream userData=readData("ontologies/test/alan.owl"); 
        userId=ontocore.addUser(userData);
          
        //add config
                            
        String confId="";          
        userData=readData("ontologies/test/alan-config.owl"); 
        confId=ontocore.addDeviceConfiguration(userData);
        
        ByteArrayInputStream request=readData("ontologies/test/rasb-lightagent-connection-changed.owl"); 
        String out=toStringOntology(ontocore.parseRequest(request))[0];
        request=readData("ontologies/test/test-upd.owl"); 
        out=toStringOntology(ontocore.parseRequest(request))[0];        
        ontocore.modifyDevice("http://www.dmi.unict.it/lightagent.owl");
                 
        //The request
        request=readData("ontologies/test/user-request-bis.owl");  
        out=toStringOntology(ontocore.parseRequest(request))[0];
        writeDown("modifyDeviceRequest", out);    
        
        //REMOVE
       
       
        try 
          {               
              ontocore.removePermanentConfigurationFromDevice(confId);
              ontocore.removePermanentUser(userId);
              ontocore.removePermanentDevice("http://www.dmi.unict.it/lightagent.owl");
              ontocore.refreshDataRequest();
              ontocore.refreshDataBehavior();
          } 
          catch (Exception ex) 
          {
              System.out.println("Error deleting user");
          } 
        // Flush
        
    }
    
    
    @AfterClass
    public static void delFiles()
    {
     
      File[] exception=new File[]{new File("oasis.owl"), new File("oasis-abox.owl")};
      emptyFolder(ontocore.getBackupPath().toFile(), new File[0]);
      emptyFolder(ontocore.getOntologiesDeviceConfigurationPath().toFile(), new File[0]);
      emptyFolder(ontocore.getOntologiesDevicesPath().toFile(), new File[0]);
      emptyFolder(ontocore.getMainOntologiesPath().toFile(), exception);
      emptyFolder(ontocore.getSatellitePath().toFile(), new File[0]);
      emptyFolder(ontocore.getOntologiesUsersPath().toFile(), new File[0]);
    }
    
    public static void emptyFolder(File dir, File[]exception)
    {
      File[]l=dir.listFiles();
      for(int i=0;i<l.length;i++)
      {
          int j=0;
          for(;j<exception.length;j++)
          {
            if(l[i].getName().equals(exception[j].getName()))
                break;
          }
          if(j==exception.length)
              l[i].delete();
      }
    }

    public void writeDown(String m, String s)
    {
        BufferedWriter writer = null;
        try {
            File filesource = new File("ontologies" + File.separator + "outTest" + File.separator+ m + ".owl");
            filesource.getParentFile().mkdirs();
            writer = new BufferedWriter(new FileWriter(filesource));
            writer.write(s);
            writer.close();
        } catch (IOException ex)
        {
            Logger.getLogger(test.class.getName()).log(Level.SEVERE, null, ex);
        } 
        finally
        {
            try 
            {
                writer.close();
            } catch (IOException ex) {
                Logger.getLogger(test.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
            
    
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    
    
    public static String[] toStringOntology(ByteArrayOutputStream[] array)
      {           
        String[] ret = new String[array.length];                   
            try
              { 
                for (int i = 0; i < ret.length; i++)
                 {    
                   if(array[i]!=null)
                     {                      
                      ret[i]=array[i].toString("UTF-8");
                     }
                 }
              } 
            catch (UnsupportedEncodingException ex)
              {                
                for (int j = 0; j < ret.length; j++)
                    ret[j]=null;
                return ret;
              }          
        return ret;
      }
      
    public static String readQuery(String path)
      {
        String query="";
         try
          {
            BufferedReader queryReader=new  BufferedReader(new FileReader(path));
            String currentLine="";
            while ((currentLine = queryReader.readLine()) != null)
              {
                  query+=currentLine;
              }
          } 
        catch (FileNotFoundException ex)
          {
            Logger.getLogger(test.class.getName()).log(Level.SEVERE, null, ex);
          }
        catch (IOException ex)
          {
            Logger.getLogger(test.class.getName()).log(Level.SEVERE, null, ex);
          }
      
        return query;
      }
    
    public static ByteArrayInputStream readData(String file)
    {
        ByteArrayInputStream inputstream=null;
        String input="";
        try {
            FileReader fileReader =  new FileReader(file);
            BufferedReader bufferedReader =  new BufferedReader(fileReader);
            String line;
            while((line = bufferedReader.readLine()) != null)
            {
                input+=line;
            }       
            fileReader.close();
            bufferedReader.close();
            inputstream=new ByteArrayInputStream(input.getBytes());
            inputstream.close();
        }
        catch (IOException ex) {
            Logger.getLogger(test.class.getName()).log(Level.SEVERE, null, ex);
            
        } 
        return inputstream;
    }
    
}

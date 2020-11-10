/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dmi.unict.it.clara.test;

import dmi.unict.it.clara.core.Clara;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

/**
 *
 * @author Daniele Francesco Santamaria
 */
public class mainAutoinstall
  {
        
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
            Logger.getLogger(mainAutoinstall.class.getName()).log(Level.SEVERE, null, ex);
          }
        catch (IOException ex)
          {
            Logger.getLogger(mainAutoinstall.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(mainAutoinstall.class.getName()).log(Level.SEVERE, null, ex);
            
        } 
        return inputstream;
    }
    
    public static void main(String[] args)
      {        
        File ontoFile=new File("ontologies/main/oasis.owl");
        File aboxFile=new File("ontologies/main/oasis-abox.owl");
       // File dataFile=new File("ontologies/main/dataset.owl");
        Clara ontocore=new Clara();
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
            ontocore.loadDevicesFromPath(true); //use this if the devices folder is not empty 
           // ontocore.startReasoner();
          } 
        catch (OWLOntologyCreationException | OWLOntologyStorageException | IOException ex)
          {
            Logger.getLogger(mainAutoinstall.class.getName()).log(Level.SEVERE, null, ex);
          }
                   
         
        //Device
        String id= "dev"+ new Timestamp(new Date().getTime()).toString().replace(" ","").replace(":","").replace(".","");
        //Stream<OWLAxiom> axioms=Stream.empty();
        String userId="ALAN";
        
       
         
       InputStream assistantData=readData("ontologies/test/homeassistant.owl"); 
       ontocore.addDevice(assistantData);            
       ontocore.addDevice("http://www.dmi.unict.it/homeassistant.owl");
     
    
       // System.out.println(ontocore.updateOntology("http://www.dmi.unict.it/profonto-home.owl",false,false));
           
       
       
      // InputStream load=readData("ontologies/test/lightagent.owl");
     //  ontocore.parseRequest(load);
     //  System.out.println("Loaded template");
       //InputStream request=readData("ontologies/test/light-installation-request-IRI.owl");
       System.out.println("Loading test");
       
        ontocore.parseRequest(readData("ontologies/test/lightagent-from-template.owl"));    
        ontocore.parseRequest( readData("ontologies/test/rasb-lightagent.owl")); 
        System.out.println("Adding light agent");
        ontocore.addDevice("http://www.dmi.unict.it/lightagent.owl");
       System.out.println("Update Request- Loading Data");
       ontocore.parseRequest(readData("ontologies/test/rasb-lightagent-toUpdateIntest.owl"));
//        System.out.println("Update Request");
        String s=toStringOntology(ontocore.parseRequest(readData("ontologies/test/light-update-request.owl")))[0];
        System.out.println("Request of device:\n" + s);
       s=null;
       ontocore.modifyDevice("http://www.dmi.unict.it/lightagent.owl");
//         
        System.out.println("Modify connection: "+ ontocore.modifyConnection("http://www.dmi.unict.it/homeassistant.owl",
                                  "192.168.0.1", "8087"));
//         
  //     InputStream request=readData("ontologies/test/rasb/test.owl");
 //      Stream<OWLAxiom> res= ontocore.parseRequest(request).axioms();
//       System.out.println("Request:");
//       if(res!=null)
//           {
//         res.forEach(System.out::println);        
//        
//         System.out.println();
 //        System.out.println("Retrieve data:");
 //        Stream<OWLAxiom> res=ontocore.retrieveBeliefAssertions("http://www.dmi.unict.it/light-installation-request.owl#light-installation-req-task");
                                    
//         res.forEach(System.out::println);
//           }
//         else System.out.println("Request unsatisfiable");              
       
          
       
       System.out.println("Add user request");
       ontocore.parseRequest(readData("ontologies/test/alan.owl"));
       ByteArrayInputStream request=readData("ontologies/test/add-user-request.owl");         
       s =  toStringOntology(ontocore.parseRequest(request))[0];
       System.out.println("Request of device: \n"+s);
       s=null;
       
       System.out.println("Add user configuration request");
       request=readData("ontologies/test/add-user-configuration-request.owl");         
       s= toStringOntology(ontocore.parseRequest(request))[0];       
       System.out.println("Request of device: \n"+s);
       s=null;
//                    
//       
//       System.out.println("Remove user configuration request");
//       request=readData("ontologies/test/remove-user-configuration-request.owl");         
//       s= toStringOntology(ontocore.parseRequest(request))[0];      
//       System.out.println("Request of device: \n"+s);
//       s=null;
//       
//       System.out.println("Testing belief with referTo object-property");
//       request=readData("ontologies/test/add-belief-refers.owl"); 
//       s= toStringOntology(ontocore.parseRequest(request))[1];
//       System.out.println(s);
//       s=null;
//       
//       System.out.println("Add belief request");
//       request=readData("ontologies/test/add-belief-request.owl");         
//       s= toStringOntology(ontocore.parseRequest(request))[0];
//       System.out.println("Request of device: \n"+ s);
//       s=null;
//       
//       System.out.println("Retrieve belief request");
//       request=readData("ontologies/test/retrieve-belief-request.owl");         
//       s= toStringOntology(ontocore.parseRequest(request))[0];
//       System.out.println("Request of device: \n"+s);
//              
//       System.out.println("Remove belief request");
//       request=readData("ontologies/test/remove-belief-request.owl");         
//       s= toStringOntology(ontocore.parseRequest(request))[0];
//       System.out.println("Request of device:\n"+s);
//       s=null;
//              
//       System.out.println("Remove user request");
//       request=readData("ontologies/test/remove-user-request.owl");         
//       s=toStringOntology(ontocore.parseRequest(request))[0];
//       System.out.println("Request of device:\n"+s);   
       
      }
  }

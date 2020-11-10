/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dmi.unict.it.clara.main;

import dmi.unict.it.clara.core.Clara;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import py4j.GatewayServer;

/**
 *
 * @author Daniele Francesco Santamaria
 */
public class ClaraEntryPoint
  {
    static Clara ontocore; //the ontological core
    /**
     * Start Profonto gateway with default configuration
     * @param args
     */
    public static void main(String[] args)
      {
        GatewayServer gatewayServer = new GatewayServer(new ClaraEntryPoint());
        gatewayServer.start();
        System.out.println("Prof-Onto's core has been started. Wait for Prof-Onto to start.");
      }

    //return a string representation of the given ontology
    private static String getStringFromOntology(OWLOntology res)
      {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if (res != null)
          {
            try
              {
                res.saveOntology(new RDFXMLDocumentFormat(), out);
                if (out != null)
                  {
                    out.close();
                  }
                return out.toString();
              } catch (OWLOntologyStorageException | IOException ex)
              {
                Logger.getLogger(ClaraEntryPoint.class.getName()).log(Level.SEVERE, null, ex);
                return "";
              }
          }
        return "";
      }

    /**
     * Create a standard entry point for the ontological core
     */
    public ClaraEntryPoint()
      {
        File ontoFile = new File("ontologies/main/oasis.owl");
        File aboxFile = new File("ontologies/main/oasis-abox.owl");
        // File dataFile=new File("ontologies/main/dataset.owl");
        ontocore = new Clara();
        try
          {
            //initialize all the used paths
            ontocore.setOntologiesDeviceConfigurationsPath(Paths.get("ontologies" + File.separator + "devConfigs"));
            ontocore.setOntologiesDevicesPath(Paths.get("ontologies" + File.separator + "devices"));
            ontocore.setMainOntologiesPath(Paths.get("ontologies" + File.separator + "main"));
            ontocore.setOntologiesUsersPath(Paths.get("ontologies" + File.separator + "users"));
            ontocore.setQueryPath(Paths.get("ontologies" + File.separator + "queries"));
            ontocore.setSatellitePath(Paths.get("ontologies" + File.separator + "satellite"));
            ontocore.setBackupPath(Paths.get("ontologies" + File.separator + "backup"));
            //set the ontologies used by the ontological core
            ontocore.setMainOntology(ontoFile);            ontocore.setMainAbox(aboxFile);

            ontocore.setDataBehaviorOntology("http://www.dmi.unict.it/prof-onto-behavior.owl", "behavior.owl");
            ontocore.setDataRequestOntology("http://www.dmi.unict.it/prof-onto-request.owl", "request.owl");
            ontocore.setDataBeliefOntology("http://www.dmi.unict.it/prof-onto-belief.owl", "belief.owl");
            ontocore.loadDevicesFromPath(true); //use this if the devices folder is not empty in order to install the devices inside
            // ontocore.startReasoner();
          } 
        catch (OWLOntologyCreationException | OWLOntologyStorageException | IOException ex)
          {
            
          }
      }

    /**
     * Check for the installation of the given device IRI
     * @param uridevice IRI of the device to be checked
     * @return 1 if the device is installed, 0 otherwise
     */
    public static int checkDevice(String uridevice)
      {
        return ontocore.checkDeviceInstallation(uridevice);
      }

    /**
     * Return a ByteArrayInputStream of the give ontology in string format
     * @param description The ontology represented as string
     * @return the ByteArrayInputStream corresponding to the given ontology
     */
    public static ByteArrayInputStream getInputStream(String description)
      {
        ByteArrayInputStream inputstream = null;
        inputstream = new ByteArrayInputStream(description.getBytes());
        try
          {
            inputstream.close();
          } 
        catch (IOException ex)
          {
            return null;
          }
        return inputstream;
      }

    /**
     * Install a device from its ontology
     * @param description the string representation of the onotology  or the IRI of the device
     * @return  the IRI of the installed device, NULL otherwise
     */
    public static String addDevice(String description)
      {
        if (description.startsWith("http") || description.startsWith("www"))
          {
            return ontocore.addDevice(description);
          }
        return ontocore.addDevice(getInputStream(description));
      }

    /**
     * Update a device with its new ontology description
     * @param description the string representation of the device or the IRI of the device
     * @return the IRI of the updated device, NULL otherwise
     */
    public static String modifyDevice(String description)
      {
        if (description.startsWith("http") || description.startsWith("www"))
          {
            return ontocore.modifyDevice(description);
          }
        return null;
      }

    /**
     * Return the address and port of the given IRI device
     * @param device the IRI of the device
     * @return a String[] where String[0] contains the address, and String[1] the port of the device
     */
    public String[] getConnectionInfo(String device)
      {
        return ontocore.getConnectionInfo(device);
      }

    /**
     * Return the string representation of the ontology from the given IRI
     * @param device the IRI of the device
     * @return the string representation of the device
     */
    public static String getDeviceOntology(String device)
      {
        OWLOntology res = ontocore.getDevice(device);
        return getStringFromOntology(res);
      }

    /**
     * Add a user from its ontology or IRI
     * @param description the string representation of the user ontology or the IRI
     * @return the IRI of the added user
     */
    public static String addUser(String description)
      {
        if (description.startsWith("http") || description.startsWith("www"))
          {
            return ontocore.addUser(description);
          }
        return ontocore.addUser(getInputStream(description));
      }

    /**
     * Add a user configuration from the given string representation of ontology or  from the IRI
     * @param description the string representation of the ontology or the IRI
     * @return the IRI of the added configuration
     */
    public static String addConfiguration(String description)
      {
        String value = null;
        if (description.startsWith("http") || description.startsWith("www"))
          {
            value = ontocore.addDeviceConfiguration(description);
          } else
          {
            value = ontocore.addDeviceConfiguration(getInputStream(description));
          }
        return value;
      }

    /**
     * Remove a user from the given ontology IRI
     * @param id the IRI of the ontology
     * @return 1 if the user has been correctly removed, -1 if some errors occur
     */
    public static int removeUser(String id)
      {
        try
          {
            ontocore.removePermanentUser(id);
          }
        catch (OWLOntologyStorageException | OWLOntologyCreationException | IOException ex)
          {
            //Logger.getLogger(ProfontoEntryPoint.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
          }
        return 1;
      }

    /**
     * Remove a device from the given IRI
     * @param id the IRI of the device
     * @return 1 if the device has been correctly removed, -1 if some errors occur
     */
    public static int removeDevice(String id)
      {        
          return  ontocore.removePermanentDevice(id);        
      }

    /**
     * Modify the address and port of the given device IRI
     * @param iri the IRI of the device to update
     * @param address the new address of the device
     * @param port the new port of the device
     * @return 1 if the device has been correctly updated, 0 otherwise
     */
    public static int modifyConnection(String iri, String address, String port)
      {
        return ontocore.modifyConnection(iri, address, port);
      }

    /**
     * Add a belief information from its ontology represented as string
     * @param input the string representation of the ontology
     * @return 1 if the belief has been correctly added, 0 otherwise
     */
    public static int addDataBelief(String input)
      {
        return ontocore.addDataToDataBelief(getInputStream(input));
      }
    
     public static int addDataRequest(String input)
      {
        return ontocore.addDataToDataRequest(getInputStream(input));
      }

    /**
     * Remove a belief information from its ontology represented as string
     * @param input the string representation of the belief
     * @return 1 if the belief has been correctly removed, 0 otherwise
     */
    public static int removeDataBelief(String input)
      {
        try
          {
            return ontocore.removeDataFromDataBelief(getInputStream(input));
          } 
        catch (OWLOntologyCreationException ex)
          {
            //Logger.getLogger(ProfontoEntryPoint.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
          }
      }

    /**
     * Return the ontology presented as string of the given belief information
     * @param input the string representation of the belief
     * @return the ontology represented as string if the belief exists, null otherwise
     */
    public String retrieveDataBelief(String input)
      {
        try
          {
            Stream<OWLAxiom> res = ontocore.retrieveDataBelief(getInputStream(input));
            if (res != null)
              {
                StringBuilder out = new StringBuilder();
                res.forEach(x -> out.append(x.toString()).append("\n"));
                return out.toString();
              }
            return null;
          } 
        catch (OWLOntologyCreationException ex)
          {
            //Logger.getLogger(ProfontoEntryPoint.class.getName()).log(Level.SEVERE, null, ex);
            return null;
          }
      }

    /**
     * Synchronyze the reasoner on the data behavior
     * @return 1 if data has been correctly synchronized, 0 otherwise
     */
    public static int syncReasonerDataBehavior()
      {
        try
          {
            ontocore.syncReasonerDataBehavior();
          } 
        catch (OWLOntologyStorageException | OWLOntologyCreationException ex)
          {
            //Logger.getLogger(ProfontoEntryPoint.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
          }
        return 1;
      }

    /**
     * Satisfies the given OASIS request 
     * @param request the OASIS request as string
     * @return a String[], where String[0] contains the execution information and String[1] satellite data
     */
    public static String[] parseRequest(String request)
      {
        ByteArrayOutputStream[] out = ontocore.parseRequest(getInputStream(request));
        String[] ret = new String[out.length];
                   
            try
              { 
                for (int i = 0; i < ret.length; i++)
                 {    
                   if(out[i]!=null)
                     {                      
                      ret[i]=out[i].toString("UTF-8");
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

    /**
     * Remove a configuration from the given configuration IRI
     * @param configuration
     * @return 1 if the configuration has been correctly removed, 0 otherwise
     */
    public static int removeConfiguration(String configuration)
      {
        try
          {
            ontocore.removePermanentConfigurationFromDevice(configuration);
          } 
        catch (OWLOntologyStorageException | OWLOntologyCreationException | IOException ex)
          {
            //Logger.getLogger(ProfontoEntryPoint.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
          }
        return 1;
      }

    /**
     * Return data associated to the given individual
     * @param individual the IRI of the individual
     * @return the ontology of the individual data as string
     */
    public static String retrieveAssertions(String individual)
      {
        Stream<OWLAxiom> res = ontocore.retrieveBeliefAssertions(individual);
        if (res != null)
          {
            StringBuilder out = new StringBuilder();
            res.forEach(x -> out.append(x.toString()).append("\n"));
            return out.toString();
          }
        return "";
      }

    /**
     * Clean up all the request previously added
     * @return 1 if all the request have been deleted, 0 otherwise
     */
    public int emptyRequest()
      {
        try
          {
            ontocore.emptyRequestOntology();
            return 1;
          } 
        catch (OWLOntologyCreationException | OWLOntologyStorageException | IOException ex)
          {
            //Logger.getLogger(ProfontoEntryPoint.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
          }
      }

    /**
     * Update the given execution status with the given value
     * @param execution the IRI of the execution to be updated
     * @param status the new status
     * @return 1 if the execution has been correctly updated, 0 otherwise, -1 if some errors occur
     */
    public int setExecutionStatus(String execution, String status)
      {
        try
          {
            ontocore.setExecutionStatus(execution, status);
          } 
        catch (OWLOntologyStorageException ex)
          {
            //Logger.getLogger(ProfontoEntryPoint.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
          }
        return 1;
      }
  }

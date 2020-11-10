/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dmi.unict.it.clara.core;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;
import javafx.util.Pair;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.junit.jupiter.api.parallel.Execution;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.RemoveImport;
import org.semanticweb.owlapi.model.parameters.ChangeApplied;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntologyAlreadyExistsException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.InferredAxiomGenerator;
import org.semanticweb.owlapi.util.InferredClassAssertionAxiomGenerator;
import org.semanticweb.owlapi.util.InferredDataPropertyCharacteristicAxiomGenerator;
import org.semanticweb.owlapi.util.InferredEquivalentClassAxiomGenerator;
import org.semanticweb.owlapi.util.InferredEquivalentDataPropertiesAxiomGenerator;
import org.semanticweb.owlapi.util.InferredEquivalentObjectPropertyAxiomGenerator;
import org.semanticweb.owlapi.util.InferredIndividualAxiomGenerator;
import org.semanticweb.owlapi.util.InferredInverseObjectPropertiesAxiomGenerator;
import org.semanticweb.owlapi.util.InferredObjectPropertyCharacteristicAxiomGenerator;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;
import org.semanticweb.owlapi.util.InferredSubClassAxiomGenerator;
import org.semanticweb.owlapi.util.InferredSubDataPropertyAxiomGenerator;
import org.semanticweb.owlapi.util.InferredSubObjectPropertyAxiomGenerator;
import org.semanticweb.owlapi.util.OWLEntityRenamer;
import org.semanticweb.owlapi.util.SimpleIRIMapper;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import ru.avicomp.ontapi.OntologyModel;

/**
 *
 * @author Daniele Francesco Santamaria
 */
public class Clara extends OntologyCore
{
    private OWLOntology mainAbox;
    private final HashMap<String, String> devices; //Hashmap for devices, <IDdevice, IDOntology>
    private final HashMap<String, String[]> devConfig; //Hashmap for user configurations.  <IDdevice,  IDOntology, IDuser>
    private final HashMap<String, String> users; //Hashmap for users. <IDdevice, IDOntology> 
    private final HashMap<String, String> queries; //Hashmap for queries file. <FileName, content>
    private final HashMap<String, String> satellite; //Hshmap for satellite data. <filename, idontology>
    
    private Pair<String, OWLOntology> databehavior; //ontology for device behaviors
    private Pair<String, OWLOntology> databelief; //ontology for databelief
    private Pair<String, OWLOntology> datarequest; //ontology for requests
    private String assistantIRI;
    private final Configuration configuration; //reasoner configuration
    private final List<InferredAxiomGenerator<? extends OWLAxiom>> generators;//reasoner configuration for inferences

    /**
     * Initialize Prof-Onto
     */
    public Clara()
    {
        super("ProfontoLog");
        devices = new HashMap<>();
        devConfig = new HashMap<>();
        users = new HashMap<>();
        queries=new HashMap<>(); 
        satellite = new HashMap<>();
        int paths = 7;
        configuration = new Configuration(paths);
        databehavior = null;
        databelief = null;
        datarequest = null;
        assistantIRI = null;
        
        mainAbox=null;        
        generators = new ArrayList<>();
        setDefaultReasonerGenerators(generators);
    }

    /**
     * Set the main file for instances
     * @param inputFile the file of the ontology
     * @throws OWLOntologyCreationException
     */
    public void setMainAbox(File inputFile) throws OWLOntologyCreationException 
      {        
        mainAbox= this.getMainManager().loadOntologyFromOntologyDocument(inputFile);
      } 
    
    /**
     * Return the ontology for instances
     * @return the ontology for instances
     */
    public OWLOntology getMainAbox() 
      {        
        return mainAbox;
      } 
    
    private void setDefaultReasonerGenerators(List<InferredAxiomGenerator<? extends OWLAxiom>> generators)
    {
        generators.add(new InferredSubClassAxiomGenerator());
        generators.add(new InferredClassAssertionAxiomGenerator());
        generators.add(new InferredDataPropertyCharacteristicAxiomGenerator());
        generators.add(new InferredEquivalentClassAxiomGenerator());
        generators.add(new InferredEquivalentDataPropertiesAxiomGenerator());
        generators.add(new InferredEquivalentObjectPropertyAxiomGenerator());
        generators.add(new InferredInverseObjectPropertiesAxiomGenerator());
        generators.add(new InferredObjectPropertyCharacteristicAxiomGenerator());
        // NOTE: InferredPropertyAssertionGenerator significantly slows down
        // inference computation
       // generators.add(new org.semanticweb.owlapi.util.InferredPropertyAssertionGenerator());
        generators.add(new InferredSubClassAxiomGenerator());
        generators.add(new InferredSubDataPropertyAxiomGenerator());
        generators.add(new InferredSubObjectPropertyAxiomGenerator());
        
        List<InferredIndividualAxiomGenerator<? extends OWLIndividualAxiom>> individualAxioms = new ArrayList<>();
        generators.addAll(individualAxioms);
     //   generators.add(new InferredDisjointClassesAxiomGenerator()); //THIS ENTRY IS PROBLEMATIC
    }

    private Configuration getConfiguration()
    {
        return configuration;
    }

    /**
     * Set the path of the folder containging the main ontologies
     * @param path path of the folder
     */
    public void setMainOntologiesPath(Path path)
    {
        this.getConfiguration().getPaths()[0] = path;
        createFolder(path);
    }

    /**
     * Load ontology taking care of dangerous situations
     * @param input the InputStream of the ontology
     * @return The corresponding OWL ontology
     */
     
    public OWLOntology secureLoadOntology(InputStream input)
    {
      try
      {        
        return this.getMainManager().loadOntologyFromOntologyDocument(input);
      }     
      catch(OWLOntologyAlreadyExistsException ex)
      {        
        return this.getMainManager().getOntology(ex.getOntologyID());      
      }
      catch (OWLOntologyCreationException ex)
      {
         return null;   
      }
    }
    
     /**
     * Load ontology taking care of dangerous situations
     * @param input the file of the ontology
     * @return The corresponding OWL ontology
     */
    public OWLOntology secureLoadOntology(File input)
    {
      try
      {
        return this.getMainManager().loadOntologyFromOntologyDocument(input);
      }     
      catch(OWLOntologyAlreadyExistsException ex)
      {        
         return this.getMainManager().getOntology(ex.getOntologyID());
      }
      catch (OWLOntologyCreationException ex)
      {
         return null;   
      }
    }
    
     /**
     * Load ontology taking care of dangerous situations
     * @param input the string representing of the ontology
     * @return The corresponding OWL ontology
     */
    public OWLOntology secureLoadOntology(String input)
    {
      try
      {
        return this.getMainManager().loadOntologyFromOntologyDocument(IRI.create(input));
      }     
      catch(OWLOntologyAlreadyExistsException ex)
      {        
         return this.getMainManager().getOntology(ex.getOntologyID());
      }
      catch (OWLOntologyCreationException ex)
      {
         return null;   
      }
    }
    
    /**
     * Return the path of the main ontologies
     * @return the path of the main ontologies
     */
    public Path getMainOntologiesPath()
    {
        return this.getConfiguration().getPaths()[0];
    }

    /**
     * Set the path of the device ontologies
     * @param path the path of the device ontologies
     */
    public void setOntologiesDevicesPath(Path path)
    {
        this.getConfiguration().getPaths()[1] = path;
        createFolder(path);
    }

    /**
     * Return the path of the device ontologies
     * @return the path of the device ontologies
     */
    public Path getOntologiesDevicesPath()
    {
        return this.getConfiguration().getPaths()[1];
    }

    /**
     * Set the path of the device configuration ontologies
     * @param path the path of the device configuration ontologies
     */
    public void setOntologiesDeviceConfigurationsPath(Path path)
    {
        this.getConfiguration().getPaths()[2] = path;
        createFolder(path);
    }

    /**
     * return the path of the device configuration ontologies
     * @return the path of the device configuration ontologies
     */
    public Path getOntologiesDeviceConfigurationPath()
    {
        return this.getConfiguration().getPaths()[2];
    }

    /**
     * Set the path of the user ontologies
     * @param path the path of the user ontologies
     */
    public void setOntologiesUsersPath(Path path)
    {
        this.getConfiguration().getPaths()[3] = path;
        createFolder(path);
    }
    
    /**
     * Return the path of the user ontologies
     * @return
     */
    public Path getOntologiesUsersPath()
    {
        return this.getConfiguration().getPaths()[3];
    }
  
    /**
     * Set the path of the SPARQL query files
     * @param path of the SPARQL query files
     */
    public void setQueryPath(Path path)
    {
       this.getConfiguration().getPaths()[4]=path; 
       for(File f : path.toFile().listFiles())
         {
           try
             {
               String name=f.getName();
               String content=readQuery(f.getCanonicalPath());                
               getQueries().put(name, content);
             } 
           catch (IOException ex)
             {
               getLogger().log(Level.SEVERE, null, ex);
             }
         }    
    }
    
    /**
     * Return the query hashmap
     * @return the query hashmap
     */
    public HashMap<String,String> getQueries()
      {
        return this.queries;
      }
    
    /**
     * Return the query path
     * @return the query path
     */
    public Path getQueryPath()
    {
      return this.getConfiguration().getPaths()[4];
    }
    
    /**
     * Return the path of the satellite ontologies
     * @return the path of the satellite ontologies
     */
    public Path getSatellitePath()
    {
      return this.getConfiguration().getPaths()[5];
    }

    /**
     * Return the satellite ontologies path
     * @param path the path of the satellite ontologies
     */
    public void setSatellitePath(Path path)
    {
        this.getConfiguration().getPaths()[5] = path;
        createFolder(path);
    }
   
    /**
     * Set the path of the backup ontologies
     * @param path the path of the backup ontologies
     */
    public void setBackupPath(Path path)
    {
        this.getConfiguration().getPaths()[6] = path;
        createFolder(path);
    }
    
    /**
     * Return the path of the backup ontologies
     * @return path the path of the backup ontologies
     */
    public Path getBackupPath()
    {
      return this.getConfiguration().getPaths()[6];
    }
    
    /**
     * Create the folder from the given path if it does not exist
     *
     * @param path the path of the folder
     */
    public void createFolder(Path path)
    {
        File directory = path.toFile();
        if (!directory.exists())
        {
            directory.mkdirs();
        }
    }

    /**
     * Set the data-belief ontology from file
     *
     * @param inputFile the file serializing the ontology
     * @throws OWLOntologyCreationException
     * @throws org.semanticweb.owlapi.model.OWLOntologyStorageException
     */
    public void setDataBeliefOntology(File inputFile) throws OWLOntologyCreationException, OWLOntologyStorageException
    {
        databelief = new Pair(this.getMainOntologiesPath() + File.separator + inputFile.getName(), this.getMainManager().loadOntologyFromOntologyDocument(inputFile));
    }

    
    /**
     * Set the data-request ontology from file
     *
     * @param inputFile the file serializing the ontology
     * @throws OWLOntologyCreationException
     * @throws org.semanticweb.owlapi.model.OWLOntologyStorageException
     */
    public void setDataRequestOntology(File inputFile) throws OWLOntologyCreationException, OWLOntologyStorageException
    {
        datarequest = new Pair(this.getMainOntologiesPath() + File.separator + inputFile.getName(), this.getMainManager().loadOntologyFromOntologyDocument(inputFile));
    }
    
       
    /**
     * Set the data-behavior ontology from file
     *
     * @param inputFile the file serializing the ontology
     * @throws OWLOntologyCreationException
     * @throws org.semanticweb.owlapi.model.OWLOntologyStorageException
     */
    public void setDataBehaviorOntology(File inputFile) throws OWLOntologyCreationException, OWLOntologyStorageException
    {
        databehavior = new Pair(this.getMainOntologiesPath() + File.separator + inputFile.getName(), this.getMainManager().loadOntologyFromOntologyDocument(inputFile));
    }

    /**
     * Sets the data-belief ontology from file name
     *
     * @param iri The IRI of the ontology
     * @param name the name of the file serializing the ontology
     * @throws OWLOntologyCreationException
     * @throws org.semanticweb.owlapi.model.OWLOntologyStorageException
     * @throws java.io.IOException
     */
    public void setDataBeliefOntology(String iri, String name) throws OWLOntologyCreationException, OWLOntologyStorageException, IOException
    {
        OWLOntology dt = this.getMainManager().createOntology(IRI.create(iri));
        File inputFile = new File(this.getMainOntologiesPath() + File.separator + name);
        this.getMainManager().saveOntology(dt, new OWLXMLDocumentFormat(), IRI.create(inputFile));
        setDataBeliefOntology(inputFile);
        //addImportToOntology(this.getDataBehaviorOntology(), this.getMainOntology().getOntologyID().getOntologyIRI().get());
        addImportToOntology(this.getDataBeliefOntology(), this.getDataRequestOntology().getOntologyID().getOntologyIRI().get());
    }

    
      /**
     * Sets the data-request ontology from file name
     *
     * @param iri The IRI of the ontology
     * @param name the name of the file serializing the ontology
     * @throws OWLOntologyCreationException
     * @throws org.semanticweb.owlapi.model.OWLOntologyStorageException
     * @throws java.io.IOException
     */
    public void setDataRequestOntology(String iri, String name) throws OWLOntologyCreationException, OWLOntologyStorageException, IOException
    {
        OWLOntology dt = this.getMainManager().createOntology(IRI.create(iri));
        File inputFile = new File(this.getMainOntologiesPath() + File.separator + name);
        this.getMainManager().saveOntology(dt, new OWLXMLDocumentFormat(), IRI.create(inputFile));
        Clara.this.setDataRequestOntology(inputFile);
        addImportToOntology(this.getDataRequestOntology(), this.getMainOntology().getOntologyID().getOntologyIRI().get());
        addImportToOntology(this.getDataRequestOntology(), this.getDataBehaviorOntology().getOntologyID().getOntologyIRI().get());
    }
    
    /**
     * Empty the request ontology
     * @throws OWLOntologyCreationException
     * @throws OWLOntologyStorageException
     * @throws IOException
     */
    public void emptyRequestOntology() throws OWLOntologyCreationException, OWLOntologyStorageException, IOException
    {
       String iri=this.getDataRequestOntology().getOntologyID().getOntologyIRI().get().toString();          
       this.getMainManager().removeOntology(this.getDataRequestOntology());
       this.setDataRequestOntology(iri, new File(this.getDataRequestInfo().getKey()).getName());         
    }
    
    
    /**
     * Set the data-beheavior ontology from file name
     *
     * @param iri The IRI of the ontology
     * @param name the name of the file serializing the ontology
     * @throws OWLOntologyCreationException
     * @throws org.semanticweb.owlapi.model.OWLOntologyStorageException
     * @throws java.io.IOException
     */
    public void setDataBehaviorOntology(String iri, String name) throws OWLOntologyCreationException, OWLOntologyStorageException, IOException
    {
        OWLOntology dt = this.getMainManager().createOntology(IRI.create(iri));
        File inputFile = new File(this.getMainOntologiesPath() + File.separator + name);
        this.getMainManager().saveOntology(dt, new OWLXMLDocumentFormat(), IRI.create(inputFile));
        setDataBehaviorOntology(inputFile);
        addImportToOntology(this.getDataBehaviorOntology(), this.getMainOntology().getOntologyID().getOntologyIRI().get());
    }

    /**
     * Import an ontology into the given ontology
     *
     * @param ontology The ontology to which add the import axiom
     * @param iri The iri to import into the dataset ontology
     * @throws OWLOntologyStorageException
     * @throws OWLOntologyCreationException
     */
    public void addImportToOntology(OWLOntology ontology, IRI iri) throws OWLOntologyStorageException, OWLOntologyCreationException
    {
        OWLImportsDeclaration importDeclaration = this.getDataFactory().getOWLImportsDeclaration(iri);
        this.getMainManager().applyChange(new AddImport(ontology, importDeclaration));
        this.getMainManager().saveOntology(ontology);
        this.getMainManager().loadOntology(ontology.getOntologyID().getOntologyIRI().get());
    }

    /**
     * Remove the import of an ontology from the given ontology
     *
     * @param ontology The ontology the import axiom has to be removed from.
     * @param iri The iri to remove from the dataset ontology
     * @throws OWLOntologyStorageException
     * @throws OWLOntologyCreationException
     */
    public void removeImportFromOntology(OWLOntology ontology, IRI iri) throws OWLOntologyStorageException, OWLOntologyCreationException
    {
        OWLImportsDeclaration importDeclaration = this.getMainManager().getOWLDataFactory().getOWLImportsDeclaration(iri);
        this.getMainManager().applyChange(new RemoveImport(ontology, importDeclaration));
        this.getMainManager().saveOntology(ontology);
        this.getMainManager().loadOntology(ontology.getOntologyID().getOntologyIRI().get());
    }

    /**
     * Return the behavior ontology
     *
     * @return the behavior ontology
     */
    public Pair<String, OWLOntology> getDataBehaviorInfo()
    {
        return this.databehavior;
    }

    /**
     * Return the belief ontology
     *
     * @return the belief ontology
     */
    public OWLOntology getDataBeliefOntology()
    {
        return getDataBeliefInfo().getValue();
    }

       /**
     * Return the request ontology
     *
     * @return the request ontology
     */
    public OWLOntology getDataRequestOntology()
    {
        return getDataRequestInfo().getValue();
    }
    
    
    
    
    
    /**
     * Return the belief infor
     *
     * @return the belief ontology info, i.e., the pair <Name, Ontology>
     */
    public Pair<String, OWLOntology> getDataBeliefInfo()
    {
        return this.databelief;
    }

      /**
     * Return the request ontology
     *
     * @return the request ontology
     */
    public Pair<String, OWLOntology> getDataRequestInfo()
    {
        return this.datarequest;
    }
    
        
    /**
     * Return the behavior ontology
     *
     * @return the behavior ontology
     */
    public OWLOntology getDataBehaviorOntology()
    {
        return getDataBehaviorInfo().getValue();
    }

    /**
     * Return the hashmap of  devices
     *
     * @return the HashMap of the  devices
     */
    public HashMap getDevices()
    {
        return devices;
    }

    /**
     * Return the hashmap of device configurations
     *
     * @return the HashMap containing the devices configuaration
     */
    public HashMap getDeviceConfigurations()
    {
        return devConfig;
    }

    /**
     * Return the hashmap of users
     *
     * @return the HashMap containing the users
     */
    public HashMap getUsers()
    {
        return users;
    }

    /**
     * Return the hashmap of the satellite ontologies
     * @return the hashmap of the satellite ontologies
     */
    public HashMap getSatellite()
      {
        return satellite;
      }
    /**
     * Add to the given ontology a set of axioms
     *
     * @param ontology the ontology to be extended with the given axioms
     * @param axioms the axioms to be added
     * @return  1 if data has been correctly added, -1 otherwise
     */
    public int addAxiomsToOntology(OWLOntology ontology, Stream<OWLAxiom> axioms)
    {
        ChangeApplied changes = ontology.addAxioms(axioms);
        try
        {            
            this.getMainManager().saveOntology(ontology);
           // this.getMainManager().loadOntology(ontology.getOntologyID().getOntologyIRI().get());
        } 
        catch (OWLOntologyStorageException ex)
        {
            getLogger().log(Level.SEVERE, null, ex);
            return -1;
        }
        if(changes.equals(ChangeApplied.SUCCESSFULLY))
                return 1;
        return -1;
    }

    /**
     * Remove the given set of axioms from the given ontology
     * @param ontology the ontology containing the axioms
     * @param axioms the axioms to be removed
     * @return 1 if axioms have been correctly removed, -1 otherwise
     * @throws OWLOntologyCreationException
     */
    public int removeAxiomsFromOntology(OWLOntology ontology, Stream<OWLAxiom> axioms) throws OWLOntologyCreationException
    {
        ChangeApplied changes = ontology.remove(axioms);
        try
        {            
            this.getMainManager().saveOntology(ontology);
            this.getMainManager().loadOntology(ontology.getOntologyID().getOntologyIRI().get());
        } 
        catch (OWLOntologyStorageException ex)
        {
            getLogger().log(Level.SEVERE, null, ex);
            return -1;
        }
        if(changes.equals(ChangeApplied.SUCCESSFULLY))
           return 1;
        return -1;
    }
    
    /**
     * Add the given axioms to the behavior ontology
     * @param axioms the axioms to be added
     * @throws OWLOntologyCreationException
     */
    public void addDataToDataBehavior(Stream<OWLAxiom> axioms) throws OWLOntologyCreationException
    {
        addAxiomsToOntology(this.getDataBehaviorOntology(), axioms);
    }
    
    /**
     * Verify if the given ontology has some execution status information
     * @param ontology the ontology to be checked
     * @return
     */
    public boolean checkHasExecutionStatutInfo(OWLOntology ontology)
      {
        String query=getQueryPrefix(null)+"\n"+this.getQueries().get("ask02a.sparql");          
        boolean res=false;
        try
          {
            QueryExecution execQ = this.createQuery(ontology, query);
            res= execQ.execAsk();
          } 
        catch (OWLOntologyCreationException ex)
          {
            getLogger().log(Level.SEVERE, null, ex);
            return false;
          }        
        return res;
      }
    
    /**
     * Add the content of the given ontology as InputStream to the belief ontology
     * @param ontologystring the input stream of the ontology to be added.
     * @return 1 if data have been correctly added, -1 othewise
     */
    public int addDataToDataBelief(InputStream ontologystring) 
    {
        OWLOntology ontology=null;
        try
        {
            ontology = this.getMainManager().loadOntologyFromOntologyDocument(ontologystring);
           
        } 
        catch (OWLOntologyAlreadyExistsException ex)
        {
            ontology=this.getMainManager().getOntology(ex.getOntologyID());
            getLogger().log(Level.SEVERE, null, ex);
        } 
        catch (OWLOntologyCreationException ex )                
        {
         getLogger().log(Level.SEVERE, null, ex);
        }        
        int r=-1;        
        r=addAxiomsToOntology(this.getDataBeliefOntology(), ontology.axioms());
        this.getMainManager().removeOntology(ontology);                
        return r;
    }
    
     /**
     * Remove the content of the given axioms as InputStream to the belief ontology
     * @param ontologystring the input stream of the ontology to be removed.
     * @return 1 if data have been correctly added, -1 othewise
     */
    public int removeDataFromDataBelief(InputStream ontologystring) throws OWLOntologyCreationException
    {
        OWLOntology ontology=this.secureLoadOntology(ontologystring);
        if(ontology==null)
            return -1;
        int r=-1;
       // if(checkHasExecutionStatutInfo(ontology))
       //  r= removeAxiomsFromOntology(this.getDataRequestOntology(), ontology.axioms()); 
       // else
         r= removeAxiomsFromOntology(this.getDataBeliefOntology(), ontology.axioms());   
        this.getMainManager().removeOntology(ontology);
        return r;
    }

     /**
     * Add the given axioms to the request ontology
     * @param axioms the axioms to be added.     
     * @throws org.semanticweb.owlapi.model.OWLOntologyCreationException     
     */
    public void addDataToDataRequest(Stream<OWLAxiom> axioms) throws OWLOntologyCreationException
    {
       addAxiomsToOntology(this.getDataRequestOntology(), axioms);
    } 

    public int addDataToDataRequest(InputStream ontologystring) 
    {
        OWLOntology ontology=null;
        int r=-1;
        try
        {
            ontology = this.getMainManager().loadOntologyFromOntologyDocument(ontologystring);
           
        } 
        catch (OWLOntologyAlreadyExistsException ex)
        {
            ontology=this.getMainManager().getOntology(ex.getOntologyID());
            getLogger().log(Level.SEVERE, null, ex);
            return 0;
        } 
        catch (OWLOntologyCreationException ex )                
        {
         getLogger().log(Level.SEVERE, null, ex);
         return -1;
        }        
                
        r=addAxiomsToOntology(this.getDataRequestOntology(), ontology.axioms());
        this.getMainManager().removeOntology(ontology);                
        return r;
    }
    
    //sync the reasoner on the given ontology and save inferences on the given file
    private void syncReasoner(OWLOntology ontology, String file) throws OWLOntologyStorageException
    {     
          
        ReasonerFactory rf = new ReasonerFactory();
        org.semanticweb.HermiT.Configuration config = new org.semanticweb.HermiT.Configuration();
        config.ignoreUnsupportedDatatypes = true;
        OWLReasoner reasoner = rf.createReasoner(ontology, config);
        reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY,
                InferenceType.CLASS_ASSERTIONS,
                InferenceType.OBJECT_PROPERTY_HIERARCHY,
                InferenceType.DATA_PROPERTY_HIERARCHY,
                InferenceType.OBJECT_PROPERTY_ASSERTIONS);

        boolean consistencyCheck = reasoner.isConsistent();
        if (consistencyCheck)
        {
            InferredOntologyGenerator iog = new InferredOntologyGenerator(reasoner, generators);
            iog.fillOntology(this.getMainManager().getOWLDataFactory(), ontology);
            reasoner.flush();
            if(!(file==null || file.equals("")))
                this.getMainManager().saveOntology(ontology,
                    IRI.create(new File(file)));
        } 
        else
        {
            System.out.println("Inconsistent Knowledge base");
        }        
    }

    /**
     * Runs the reasoner on the behavior data
     *
     * @throws OWLOntologyStorageException
     * @throws org.semanticweb.owlapi.model.OWLOntologyCreationException
     */
    public void syncReasonerDataBehavior() throws OWLOntologyStorageException, OWLOntologyCreationException
    {      
        OWLOntology m = this.getDataBehaviorOntology();
        m.imports().forEach(ont ->ont.axioms().forEach(a-> m.addAxiom(a)));     
        this.syncReasoner(m, this.getDataBehaviorInfo().getKey());
    }

    //clean up an ontology except for the import axioms
    private void refreshOntology(OWLOntology ontology) throws OWLOntologyStorageException, OWLOntologyCreationException
    {
        Stream<OWLImportsDeclaration> imports = ontology.importsDeclarations();
        ontology.removeAxioms(ontology.axioms());
        imports.forEach(dec -> this.getMainManager().applyChange(new AddImport(ontology, dec)));
        this.getMainManager().saveOntology(ontology);
        this.getMainManager().loadOntology(ontology.getOntologyID().getOntologyIRI().get());
    }

    /**
     * refreshes the behavior dataset
     *
     * @throws OWLOntologyStorageException
     * @throws OWLOntologyCreationException
     */
    public void refreshDataBehavior() throws OWLOntologyStorageException, OWLOntologyCreationException
    {
        refreshOntology(this.getDataBehaviorOntology());
    }
   
    /**
     * Add an user from its IRI
     * @param iri the IRI of the user
     * @return
     */
    public String addUser(String iri)
    {
        try
        {
           OWLOntology ontouser=this.getMainManager().loadOntology(IRI.create(iri)); 
           return addUser(ontouser);
        }
        catch (OWLOntologyCreationException ex)
        {
           getLogger().log(Level.SEVERE, null, ex);
            return null;
        }
       
    }
    
    /**
     * Add an user from the given ontology as InputStream
     * @param ontologyData the input stream of th ontology of the user to be added
     * @return the name of the user as string
     */
    public String addUser(InputStream ontologyData)
      {
        OWLOntology ontouser = this.secureLoadOntology(ontologyData);
        if(ontouser==null)
            return null;
        return addUser(ontouser);                     
      }
    
    /**
     * insert a new user given its ontology data
     *
     * @param ontouser the ontology of the user to be added
     * @return the name of the user as string
     */
    public String addUser(OWLOntology ontouser)
    {
        
        String val[]=new String[]{""};
        try
        {            
            this.deleteSatelliteData(ontouser);
            addImportToOntology(this.getDataBehaviorOntology(), ontouser.getOntologyID().getOntologyIRI().get()); 
            String userclass=this.getMainOntology().getOntologyID().getOntologyIRI().get().toString()+"#User";
            ontouser.logicalAxioms().filter(x->x.isOfType(AxiomType.CLASS_ASSERTION)).forEach(
              element -> {
                            OWLClassAssertionAxiom ax=((OWLClassAssertionAxiom) element);
                            if(ax.getClassExpression().asOWLClass().toStringID().equals(userclass))                              
                                val[0]=getEntityName(ax.getIndividual().asOWLNamedIndividual().toStringID());                            
                         });  
            
            
            String filesource = this.getOntologiesUsersPath() + File.separator + val[0] + ".owl";
            File file = new File(filesource);

            this.getMainManager().addIRIMapper(new SimpleIRIMapper(ontouser.getOntologyID().getOntologyIRI().get(),
                    IRI.create(file.getCanonicalFile())));

            FileOutputStream outStream = new FileOutputStream(file);

            this.getMainManager().saveOntology(ontouser, new OWLXMLDocumentFormat(), outStream);
            this.getUsers().put(val[0], ontouser.getOntologyID().getOntologyIRI().get().toString());
            outStream.close();
            //   syncReasonerDataBehavior();            
        } catch (IOException | OWLOntologyStorageException | OWLOntologyCreationException ex)
        {
            getLogger().log(Level.SEVERE, null, ex);
        }
        return val[0];
    }

    /**
     * Return the connection information of the  device
     * @param device the IRI of the device as string
     * @return a String[] containing the info of the device: String[0] contains the address, String[1] the port
     */
    public String[] getConnectionInfo(String device)
    {       
     OWLOntology ontology= this.getDevice(device); 
     String[] value={"",""};
     ontology.axioms().filter(x->x.isOfType(AxiomType.DATA_PROPERTY_ASSERTION))
                      .forEach( x  ->
     {
        OWLDataPropertyAssertionAxiom ax = ((OWLDataPropertyAssertionAxiom) x);
        if(ax.getProperty().asOWLDataProperty().toStringID().equals(this.getMainOntology().getOntologyID().getOntologyIRI().get().toString()+"#hasIPAddress"))
            value[0]= ax.getObject().getLiteral();
        else if(ax.getProperty().asOWLDataProperty().toStringID().equals(this.getMainOntology().getOntologyID().getOntologyIRI().get().toString()+"#hasPortNumber"))
            value[1]= ax.getObject().getLiteral();
     });
    return value;
    }
    
    /**
     * Verify if the given device is already installed
     * @param iridevice the IRI of the device
     * @return
     */
    public int checkDeviceInstallation(String iridevice)
      {       
        for (Map.Entry pair : devices.entrySet())          
         if((((String)pair.getValue())+"#"+((String)pair.getKey())).equals(iridevice))                    
                return 1;                   
        return 0;
      }
    
    /**
     * add a new device given its IRI
     * @param iri the iri of the device to be added
     * @return the created ontology
     */
    
    public String addDevice(String iri)
    {       
        try
        {
           OWLOntology ontodevice=this.getMainManager().loadOntology(IRI.create(iri)); 
           return addDevice(ontodevice);
        }
        catch (OWLOntologyCreationException ex)
        { 
           getLogger().log(Level.SEVERE, null, ex);
           return null;
        }
       
    }
    
    /**
     * Add a new device given its ontology as InputStream
     * @param ontologyData the ontology of the device as InputStream
     * @return the name of the device
     */
    public String addDevice(InputStream ontologyData)
    {
      OWLOntology ontology = this.secureLoadOntology(ontologyData);
      if(ontology==null)
          return null;
      return addDevice(ontology);
    }
    
    public String getAssistantIRI()
    {
      return assistantIRI;
    }
    public void setAssistantIRI(String s)
    {
      this.assistantIRI=s;
    }
    
    /**
     * Add a new device given its ontology 
     * @param ontodevice the ontology of the device 
     * @return the name of the device
     */
    public String addDevice(OWLOntology ontodevice)
    {        
        String val[]={""};         
        for (Object entry : this.getDevices().values())            
        {
          if( ((String) entry).equals(ontodevice.getOntologyID().getOntologyIRI().get().toString()))
              return "";
        }
        //  getDevices().get(ontodevice.getOntologyID().getOntologyIRI().get().toString());
        try
        {            
            this.deleteSatelliteData(ontodevice);
            addImportToOntology(this.getDataBehaviorOntology(), ontodevice.getOntologyID().getOntologyIRI().get());            
            String deviceclass=this.getMainOntology().getOntologyID().getOntologyIRI().get().toString()+"#Device";
            String adoptsTemplate=this.getMainOntology().getOntologyID().getOntologyIRI().get().toString()+"#adoptsAgentBehaviorTemplate";
            String hasBehavior = this.getMainOntology().getOntologyID().getOntologyIRI().get().toString()+"#hasBehavior";
            String individual="";
            ontodevice.logicalAxioms().filter(y->y.isOfType(AxiomType.OBJECT_PROPERTY_ASSERTION)).forEach
                                 (
                                      elementInt -> {
                                          
                                         OWLObjectPropertyAssertionAxiom opaz=((OWLObjectPropertyAssertionAxiom) elementInt);
                                         if(opaz.objectPropertiesInSignature().anyMatch(k->k.asOWLObjectProperty().toStringID().equals(adoptsTemplate) 
                                           || k.asOWLObjectProperty().toStringID().equals(hasBehavior) ) )
                                           {
                                            String  ind=opaz.getSubject().asOWLNamedIndividual().toStringID();                                             
                                            ontodevice.logicalAxioms().filter(x->x.isOfType(AxiomType.CLASS_ASSERTION)).forEach(
                                             element -> {
                                                   OWLClassAssertionAxiom ax=((OWLClassAssertionAxiom) element);
                                                    if(ax.getClassExpression().asOWLClass().toStringID().equals(deviceclass))
                                                      if(ind.equals(ax.getIndividual().asOWLNamedIndividual().toStringID()))
                                                      {
                                                          val[0]=getEntityName(ind);
                                                          if(this.getDevices().isEmpty())//assistant
                                                                this.setAssistantIRI(ind);
                                                      }                                                                                  
                                                        
                                             });                                                                                                                                                                           
                                           }
                                      }
                              );                      
                                                     
                         
            
            String filesource = this.getOntologiesDevicesPath() + File.separator + val[0] + ".owl";
            File file = new File(filesource);

            this.getMainManager().saveOntology(ontodevice, IRI.create(new File( this.getBackupPath() + File.separator + val[0] + ".owl")));
            
            this.getMainManager().getIRIMappers().add(new SimpleIRIMapper(ontodevice.getOntologyID().getOntologyIRI().get(),
                    IRI.create(file.getCanonicalFile())));

          //  FileOutputStream outStream = new FileOutputStream(file);
          syncReasoner(ontodevice, filesource);
          
         //   this.getMainManager().saveOntology(ontodevice, new OWLXMLDocumentFormat(), outStream);
            this.getDevices().put(val[0], ontodevice.getOntologyID().getOntologyIRI().get().toString());
        //    outStream.close();
            //  syncReasonerDataBehavior();

        /*  ontodevice.imports().forEach(o->{
                try {
                    addImportToOntology(this.getDataBehaviorOntology(), o.getOntologyID().getOntologyIRI().get());
                } catch (OWLOntologyStorageException ex) {
                   
                } catch (OWLOntologyCreationException ex) {
                    
                }
            });*/
            
            
        } 
        catch (IOException | OWLOntologyStorageException | OWLOntologyCreationException ex)
        {
           getLogger().log(Level.SEVERE, null, ex);
           return null;
        }
        return val[0];
    }

    /**
     * Return the ontology corresponding to the given user id
     *
     * @param id the id of the device
     * @return the ontology representing the user
     */
    public OWLOntology getUser(String id)
    {
        String tmp = (String) this.getUsers().get(id);
        return this.getMainManager().getOntology(IRI.create(tmp));
    }

    /**
     * Return the ontology corresponding to the given device id
     *
     * @param id the id of the device
     * @return the ontology representing the device
     */
    public OWLOntology getDevice(String id)
    {
        String tmp = (String) this.getDevices().get(id);
        return this.getMainManager().getOntology(IRI.create(tmp));
    }

    /**
     * Return the ontology corresponding to the given device id configuration
     *
     * @param id the id of the device configuration
     * @return the ontology representing the device configuration
     */
    public String[] getDeviceConfiguration(String id)
    {
        return (String[]) this.getDeviceConfigurations().get(id);
    }

    /**
     * Remove a given user
     *
     * @param id the id of the user to be removed
     * @throws org.semanticweb.owlapi.model.OWLOntologyStorageException
     * @throws org.semanticweb.owlapi.model.OWLOntologyCreationException
     * @throws java.io.IOException
     */
    public void removePermanentUser(String id) throws OWLOntologyStorageException, OWLOntologyCreationException, IOException
    {
        OWLOntology ontology = this.getUser(id);
        String tmp = (String) this.getUsers().remove(id); //this.getMainManager().getOntology(IRI.create(tmp));             
        this.getMainManager().removeOntology(ontology);
        removeImportFromOntology(this.getDataBehaviorOntology(), IRI.create(tmp));
        File f = new File(this.getOntologiesUsersPath() + File.separator + id + ".owl");
        this.getMainManager().getIRIMappers().remove(new SimpleIRIMapper(ontology.getOntologyID().getOntologyIRI().get(),
                IRI.create(f.getCanonicalFile())));
        f.delete(); //always be sure to close all the open streams
        removePermanentConfigurationsFromUser(id);
    }

    /**
     * Remove a given device
     *
     * @param id the id of the device to be removed
     * @return 
     */
    public int removePermanentDevice(String id) 
    {
        try
          {
            int found=0;
            for (Object entry : this.getDevices().keySet())            
              {
               if( ((String) entry).equals(id))
                 {
                   found=1;
                   break;
                 }
               }
            if (found==0)
                return 0;
            OWLOntology ontology = this.getDevice(id);
            String tmp = (String) this.getDevices().remove(id); //this.getMainManager().getOntology(IRI.create(tmp));
            this.getMainManager().removeOntology(ontology);
            removeImportFromOntology(this.getDataBehaviorOntology(), IRI.create(tmp));
            File f = new File(this.getOntologiesDevicesPath() + File.separator + id + ".owl");
            this.getMainManager().getIRIMappers().remove(new SimpleIRIMapper(ontology.getOntologyID().getOntologyIRI().get(),
                    IRI.create(f.getCanonicalFile())));
            
            f.delete(); //always be sure to close all the open streams
            new File(this.getBackupPath() + File.separator + id + ".owl").delete();
            removePermanentConfigurationsFromDevice(id);
          } 
        catch (IOException | OWLOntologyStorageException | OWLOntologyCreationException ex)
          {
            getLogger().log(Level.SEVERE, null, ex);
            return -1;
          } 
        return 1;
    }

    /**
     * Load the devices from the device installation path
     * @param toimport if True also include the imported ontology
     * @throws OWLOntologyStorageException
     * @throws IOException
     */
    public void loadDevicesFromPath(boolean toimport) throws OWLOntologyStorageException, IOException
    {
        Path path = this.getOntologiesDevicesPath();
        //HashMap<String, Pair<OWLOntology,File>>
        File[] files = path.toFile().listFiles();
        for (int i = 0; i < files.length; i++)
        {
            if (files[i].isFile())
            {
                try {
                    String name = files[i].getName();
                    
                    OWLOntology ontology = this.secureLoadOntology(files[i]);
                    if(ontology==null)
                        return;
                    this.getDevices().put(name, new Pair(ontology.getOntologyID().getOntologyIRI().get().toString(), files[i]));
                    if (toimport)
                      {
                        this.addImportToOntology(this.getDataBehaviorOntology(), ontology.getOntologyID().getOntologyIRI().get());
                        this.getMainManager().addIRIMapper(new SimpleIRIMapper(ontology.getOntologyID().getOntologyIRI().get(),
                                IRI.create(files[i].getCanonicalFile())));
                      }
                    
                    //Getting configurations from the current device
                    File confFolder = new File(this.getOntologiesDeviceConfigurationPath() + File.separator + name);
                    if (confFolder.isDirectory())
                      {
                        File[] confs = confFolder.listFiles();
                        for (int j = 0; j < confs.length; j++)
                          {
                            ontology = this.secureLoadOntology(confs[j]);//IDconfig <IDdevice, IDOntology>
                            /*attention here - need to be modified since the user is not got from the ontology*/
                            this.getDeviceConfigurations().put(confs[j].getName(), new String[]
                              {
                                name, ontology.getOntologyID().getOntologyIRI().get().toString(), "iduser"
                            });
                            if (toimport)
                              {
                                this.addImportToOntology(this.getDataBehaviorOntology(), ontology.getOntologyID().getOntologyIRI().get());
                                this.getMainManager().addIRIMapper(new SimpleIRIMapper(ontology.getOntologyID().getOntologyIRI().get(),
                                        IRI.create(confs[j].getCanonicalFile())));
                              }
                          }
                      } 
                   } 
                   catch (OWLOntologyCreationException ex) 
                     {
                      getLogger().log(Level.SEVERE, null, ex);
                     }
            }
        }

    }

    /**
     * Return the name of an individual
     * @param iri the IRI of the individual
     * @return
     */
    public String getEntityName(String iri)
      {
        return iri.substring(iri.lastIndexOf("#") + 1);
      }
    
    /**
     * Add a device configuration from its IRI
     * @param iri the iri of the device configuration
     * @return the name of the device
     */
    public String addDeviceConfiguration(String iri)
    {
        try
        {
           OWLOntology ontoconf=this.getMainManager().loadOntology(IRI.create(iri)); 
           return addDeviceConfiguration(ontoconf);
        }
        catch (OWLOntologyCreationException ex)
        {
            getLogger().log(Level.SEVERE, null, ex);
            return null;
        }
       
    }
    
    /**
     * Add a device configuration from its ontology as InputStream
     * @param deviceConfig the InputStream representing the ontology of the device configuration
     * @return the name of the device
     */
    public String addDeviceConfiguration(InputStream deviceConfig) 
      {
         OWLOntology ontodevConf = this.secureLoadOntology(deviceConfig);
         if(ontodevConf==null)
             return null;
         return addDeviceConfiguration(ontodevConf);
        
      }
    
    /**
     * Add a device configuration from its ontology
     * @param ontodevConf the ontology of the configuration
     * @return the name of the configuration
     */
    public String addDeviceConfiguration(OWLOntology ontodevConf)
    {     
      String []vals=new String[]{"","",""};  
      try
        {        
            this.deleteSatelliteData(ontodevConf);
            String settles=(this.getMainOntology().getOntologyID().getOntologyIRI().get().toString()+"#settles");
            String config=(this.getMainOntology().getOntologyID().getOntologyIRI().get().toString()+"#configurationProvidedBy");
            ontodevConf.logicalAxioms().filter(x->x.isOfType(AxiomType.OBJECT_PROPERTY_ASSERTION)).forEach(
              element -> {
                            OWLObjectPropertyAssertionAxiom ax=((OWLObjectPropertyAssertionAxiom) element);
                            if(ax.getProperty().getNamedProperty().toStringID().equals(settles))                              
                                vals[0]=getEntityName(ax.getSubject().asOWLNamedIndividual().toStringID());
                            else if(ax.getProperty().getNamedProperty().toStringID().equals(config)) 
                              {
                                 vals[1]=getEntityName(ax.getSubject().asOWLNamedIndividual().toStringID());
                                 vals[2]=getEntityName(ax.getObject().asOWLNamedIndividual().toStringID());
                                
                              }
                         });  
            
            File directory = new File(this.getOntologiesDeviceConfigurationPath() + File.separator + vals[0]);
            if (!directory.exists())
            {
             directory.mkdir();
            }
            addImportToOntology(this.getDataBehaviorOntology(), ontodevConf.getOntologyID().getOntologyIRI().get());
            String filesource = directory.getAbsolutePath() + File.separator + vals[1] + ".owl";
            File file = new File(filesource);
            FileOutputStream outStream = new FileOutputStream(file);

            this.getMainManager().getIRIMappers().add(new SimpleIRIMapper(ontodevConf.getOntologyID().getOntologyIRI().get(),
                    IRI.create(file.getCanonicalFile())));

            this.getMainManager().saveOntology(ontodevConf, new OWLXMLDocumentFormat(), outStream);
            this.getDeviceConfigurations().put(vals[1], new String[]
            {
                vals[0], ontodevConf.getOntologyID().getOntologyIRI().get().toString(), vals[2]
            });
            outStream.close();
            //    this.syncReasonerDataBehavior();

        } 
      catch (IOException | OWLOntologyStorageException | OWLOntologyCreationException ex)
        {
           // getLogger().log(Level.SEVERE, null, ex);
            return null;
        }
        return vals[1];      
    }
    
   

    /**
     * Removes a given device
     *
     * @param id the id of the device to be removed
     * @throws org.semanticweb.owlapi.model.OWLOntologyStorageException
     * @throws org.semanticweb.owlapi.model.OWLOntologyCreationException
     */
    //IDconfig <IDdevice-  IDOntology- IDuser
    public void removePermanentConfigurationFromDevice(String id) throws OWLOntologyStorageException, OWLOntologyCreationException, IOException
    {
        String[] device = (String[]) this.getDeviceConfigurations().remove(id);        
        OWLOntology ontology = this.getMainManager().getOntology(IRI.create((String) device[1]));
        this.getMainManager().removeOntology(ontology);
        removeImportFromOntology(this.getDataBehaviorOntology(), IRI.create(ontology.getOntologyID().getOntologyIRI().get().toString()));
        String file = this.getOntologiesDeviceConfigurationPath() + File.separator + (String) device[0] + File.separator + id + ".owl";
        File f = new File(file);
        this.getMainManager().getIRIMappers().remove(new SimpleIRIMapper(ontology.getOntologyID().getOntologyIRI().get(),
                IRI.create(f.getCanonicalFile())));
        f.delete(); //always be sure to close all the open streams 
        
        
        
    }

    /**
     * Removes all the configurations of a device
     *
     * @param idDevice the device id
     * @throws OWLOntologyStorageException
     * @throws OWLOntologyCreationException
     */
    public void removePermanentConfigurationsFromDevice(String idDevice) throws OWLOntologyStorageException, OWLOntologyCreationException
    {
        Iterator it = getDeviceConfigurations().entrySet().iterator(); //IDconfig <IDdevice, IDOntology, IDuser> 
        ArrayList<String> toremove = new ArrayList();
        while (it.hasNext())
        {
            Map.Entry entry = (Map.Entry) it.next();
            String[] pair = (String[]) entry.getValue();
            if (((String) pair[0]).equals(idDevice))
            {
                OWLOntology ontology = this.getMainManager().getOntology(IRI.create((String) pair[1]));
                this.getMainManager().removeOntology(ontology);
                removeImportFromOntology(this.getDataBehaviorOntology(), IRI.create(ontology.getOntologyID().getOntologyIRI().get().toString()));
                String file = this.getOntologiesDeviceConfigurationPath() + File.separator + pair[0] + File.separator
                        + (String) entry.getKey() + ".owl";

                (new File(file)).delete();
                toremove.add((String) entry.getKey());
            }
        }
        String folder = this.getOntologiesDeviceConfigurationPath() + File.separator + idDevice;
        (new File(folder)).delete();
        for (String s : toremove)
        {
            getDeviceConfigurations().remove(s);
        }
    }

    /**
     * Removes all the configurations of a user
     *
     * @param idUser the id of the user
     * @throws OWLOntologyStorageException
     * @throws OWLOntologyCreationException
     */
    public void removePermanentConfigurationsFromUser(String idUser) throws OWLOntologyStorageException, OWLOntologyCreationException
    {
        Iterator it = getDeviceConfigurations().entrySet().iterator(); //IDconfig Pair <IDdevice, <IDOntology IDuser> >
        ArrayList<String> toremove = new ArrayList();
        while (it.hasNext())
        {
            Map.Entry entry = (Map.Entry) it.next();
            String[] pair = (String[]) entry.getValue();
            String idDevice = pair[0];
            String userMatch = pair[2];
            if (userMatch.equals(idUser))
            {
                OWLOntology ontology = this.getMainManager().getOntology(IRI.create(pair[1]));
                this.getMainManager().removeOntology(ontology);
                removeImportFromOntology(this.getDataBehaviorOntology(), IRI.create(ontology.getOntologyID().getOntologyIRI().get().toString()));
                String file = this.getOntologiesDeviceConfigurationPath() + File.separator + idDevice + File.separator
                        + (String) entry.getKey() + ".owl";
                (new File(file)).delete();
                toremove.add((String) entry.getKey());
                String folder = this.getOntologiesDeviceConfigurationPath() + File.separator + idDevice;
                File f = new File(folder);
                String[] files = f.list();
                if (files.length == 0)
                {
                    f.delete();
                }

            }
        }

        for (String s : toremove)
        {
            getDeviceConfigurations().remove(s);
        }
    }

    /**
     * Read a query from the given file
     * @param file the file of the query
     * @return
     */
    public  String readQuery(String file)
      {
        String query="";
        BufferedReader queryReader=null;
         try
          {
            queryReader=new  BufferedReader(new FileReader(file));
            String currentLine="";
            while ((currentLine = queryReader.readLine()) != null)
              {
                  query+=currentLine+"\n";
              }
            queryReader.close();
          } 
        catch (IOException ex)
          {
             getLogger().log(Level.SEVERE, null, ex);   
             return null;
          }          
        return query;
      }
    
    /**
     * Performs the given query on all the dataset
     * @param query the query to be performed
     * @return the OntologyModel of the query result
     * @throws OWLOntologyCreationException
     */
    public OntologyModel performQuery(String query) throws OWLOntologyCreationException
      {
        OWLOntology ontology=this.getMainManager().createOntology();
        return this.performQuery(ontology,query);
      }
    
     /**
     * Performs the given query on the given ontology
     * @param ontology the ontology target of the query
     * @param query the query to be performed
     * @return the OntologyModel of the query result
     */
    public OntologyModel performQuery(OWLOntology ontology, String query)
      {
        OntologyModel res=null;
          try
            {        
        this.getMainManager().ontologies().forEach(x->ontology.addAxioms(x.axioms()));
        QueryExecution execQ = this.createQuery(ontology, query);
        res=this.performConstructQuery(execQ);
        this.addDataToDataRequest(res.axioms());
  //      this.getMainManager().removeOntology(ontology);
       } 
        catch (OWLOntologyCreationException | IOException ex)
        {
            getLogger().log(Level.SEVERE, null, ex);
        }
        return res;
      }
    
    
    
    //retrieve the data concerning the tasks of a request
    private ArrayList<String[]> retrieveDependencies(OWLOntology ontology, String prefix) throws OWLOntologyCreationException
      {       
        String subquery = prefix + this.getQueries().get("body01b.sparql");        
        QueryExecution execQ = this.createQuery(ontology, subquery);
        ResultSet setIRI = execQ.execSelect();
        ArrayList<String[]> depends=new ArrayList();
       
        while(setIRI.hasNext())
          {
            QuerySolution qs = setIRI.next();
            String[] entry=new String[2];
            entry[0]=qs.getResource("taska").getURI();
            entry[1]=qs.getResource("taskb").getURI();
            depends.add(entry);           
          }
        return depends;
      }
    
    /**
     * Add a set of default prefixes having as base the given IRI
     * @param IRIrequest the IRI base as string 
     * @return the prefix constructed
     */
    public String getQueryPrefix(String IRIrequest)
      {
         String prefix = this.getQueries().get("prefix01.sparql");
         prefix += "PREFIX oasis: <" + this.getMainOntology().getOntologyID().getOntologyIRI().get().toString() + "#>\n";
         String mainID = this.getMainAbox().getOntologyID().getOntologyIRI().get().toString();
         prefix += "PREFIX abox: <" + mainID + "#>\n";
         if(IRIrequest!=null)
           prefix += "PREFIX base: <" + IRIrequest + ">\n";
         return prefix;
      }
    
    /**
     * Delete from satellite data the given ontology
     * @param ont the ontology to be removed from satellite data
     */
    public void deleteSatelliteData(OWLOntology ont)
      {
        try
          {
            String key =ont.getOntologyID().getOntologyIRI().get().toString();
            String value= (String) this.getSatellite().get(key);
            if(value==null)
                return;
            
            this.getSatellite().remove(key);
            String name=value.substring(value.lastIndexOf(File.separator)+1, value.length());
            File oldfile=new File(this.getSatellitePath() + File.separator + name);            
            this.getMainManager().removeIRIMapper(new SimpleIRIMapper(ont.getOntologyID().getOntologyIRI().get(),
                                                  IRI.create(oldfile.getCanonicalFile())));                     
            oldfile.delete();
            
//            String filesource = path+ "/" + name;
//            File file = new File(filesource);          
//            FileOutputStream outStream = new FileOutputStream(file);
//            this.getMainManager().saveOntology(ont, new OWLXMLDocumentFormat(), outStream);
//            this.getSatellite().put(ont.getOntologyID().getOntologyIRI().get().toString(),filesource);
//            outStream.close();
          } 
        catch (IOException ex)
          {
            getLogger().log(Level.SEVERE, null, ex);
          }
      }
    
    /**
     * Add the given ontology to the satellite data
     * @param ontology to be added
     */
    public void addSatelliteData(OWLOntology ontology)
      {       
        try
        {            
            String name=ontology.getOntologyID().getOntologyIRI().get().toString();
            int ind=name.lastIndexOf("#"); 
            name=name.substring(name.lastIndexOf("/")+1, (ind >= 0)? ind: name.length());            
            String filesource = this.getSatellitePath() + File.separator + name;
            File file = new File(filesource);            
            this.getMainManager().getIRIMappers().add(new SimpleIRIMapper(ontology.getOntologyID().getOntologyIRI().get(),
                                                                   IRI.create(file.getCanonicalFile())));
            FileOutputStream outStream = new FileOutputStream(file);
            this.getMainManager().saveOntology(ontology, new OWLXMLDocumentFormat(), outStream);
            this.getSatellite().put(ontology.getOntologyID().getOntologyIRI().get().toString(), name);
            outStream.close();           
        } 
        catch (IOException | OWLOntologyStorageException ex)
        {
           getLogger().log(Level.SEVERE, null, ex);
           
        }      
      }
    
    /**
     * Check wheter the given ontology is a request or an attempt of adding satellite data. In the latter case the data is added.
     * @param input the ontology of the request as ByteArrayInputStream
     * @return the ontology of the request or null if it is an attempt of adding satellite data
     */
    public OWLOntology checkSatelliteData(ByteArrayInputStream input)
      {
        OWLOntology ontology=null;        
        try
        {        
            ontology=this.getMainManager().loadOntologyFromOntologyDocument(input);
            if(ontology.axioms().count()==0)
                return null;
        }     
        catch(OWLOntologyAlreadyExistsException ex)
         {   
           OWLOntologyID id=ex.getOntologyID();
           String ontoInfo[]=getPathAndName(id.getOntologyIRI().get().toString());
           if(ontoInfo==null ||ontoInfo[1]==null)
               return null;            
            try 
              {
                input.reset();
                File f=new File(this.getSatellitePath()+File.separator+ontoInfo[1]);            
                FileOutputStream outputStream = new FileOutputStream(f);
                int read;
                byte[] bytes = new byte[1024];
                while ((read = input.read(bytes)) != -1)
                   outputStream.write(bytes, 0, read); 
                this.getSatellite().put(ex.getOntologyID().getOntologyIRI().get().toString(), ontoInfo[1]);
                input.close();
                outputStream.close();
                 
              }
            catch (IOException ex1)
              {
                return null;
              }  
           return null;
        }
        catch (OWLOntologyCreationException ex)
        {
         return null;   
        } 
        
            boolean[] brequest= {false};
            ontology.axioms().filter(x->x.isOfType(AxiomType.OBJECT_PROPERTY_ASSERTION)).forEach
            (
            element -> { 
              OWLObjectPropertyAssertionAxiom ax=(OWLObjectPropertyAssertionAxiom) element;
             if( ax.getProperty().asOWLObjectProperty().toStringID().equals(this.getMainOntology().getOntologyID().getOntologyIRI().get().toString()+"#requests"))
                brequest[0]= true;
               }
             ); 
             
            if(brequest[0]==false)  //satellite
              {                 
                addSatelliteData(ontology);                
                return null; //all null values
              }   
         return ontology;
      }
    
    
    
    
    public String getDistincElemInQuerySetAsString(List<QuerySolution> q, String query)
    {
       List<String> s=this.getDistincElemInQuerySet(q, query);
       String obelemType="";
          for(int i=0;i<s.size();i++) 
          {     
            if(s.get(i).startsWith("http:"))  
              obelemType+="<"+s.get(i)+"> ";
            else
              obelemType+=s.get(i);
          }
        return obelemType;
    }
    
    public List<String> getDistincElemInQuerySet(List<QuerySolution> q, String query)
    {
      List<String> s=new ArrayList<String>();  
      for(int i=0; i<q.size();i++)
      {
          RDFNode node = q.get(i).get(query);
          int j=0;
          String value=null; 
          if(node.isResource())
              value = q.get(i).getResource(query).getURI(); 
            else
              value = q.get(i).getLiteral(query).toString();
          for(;j<s.size();j++)         
             if(value.equals(s.get(j)))
                 break;             
          if(value!=null && j==s.size())          
              s.add(value);              
      } 
     return s;
    }
   
      
    public String fromLiteralToTurtle(Literal l)
    {
       return "\""+l.getLexicalForm()+"\"^^"+"<"+l.getDatatypeURI()+">";
    }
    
    
    
    public String getTriplesFromQueryMatch(List<QuerySolution> querySol, String sub, String prop, String value)
    {
       String ret="";
       for(int i=0;i<querySol.size();i++)
           {
               
             String thepropinp = " <"+querySol.get(i).getResource(prop).getURI()+ "> ";
             String entry=sub + thepropinp;
           // System.out.println(thepropinp +" -------- "+ querySol.get(i).get(value).toString());
             if(querySol.get(i).get(value).isResource())
             {
                 entry += "<"+querySol.get(i).getResource(value).getURI()+"> .\n";
                 entry+= sub+ " rdf:type owl:NamedIndividual .\n";
                 entry+=thepropinp + " rdf:type owl:ObjectProperty .\n ";
             }
             else
             {
                 entry +=" "+ this.fromLiteralToTurtle(querySol.get(i).getLiteral(value))+" .\n";  
                 entry +=thepropinp + " rdf:type owl:DatatypeProperty .\n ";
             }
             ret+=entry;
           }
        return ret;
    }
    
    
    
    
    public String createExecEntrustGraph(List<QuerySolution> query, String base, 
             String planExecution, String goalExecution, String taskexec, String goald, String taskd, String destination)
    {               
       String ret="";
       
       ret+=planExecution+" a oasis:PlanExecution.\n";
       ret+=goalExecution+" a oasis:GoalExecution.\n";
       ret+=taskexec+" a oasis:TaskExecution.\n";
       ret+=planExecution+ " oasis:consistsOfGoalExecution "+goalExecution+" .\n";
       ret+=goalExecution+ " oasis:consistsOfTaskExecution "+taskexec+" .\n";
       ret+=goald +"  oasis:hasGoalExecution " +goalExecution+" .\n";
       ret+=taskd +"  oasis:hasTaskExecution " +goalExecution+" .\n";
       if(destination==null)
       {
         String plan="<"+base+"_planEntrustment"+">";
         String goal="<"+base+"_goalEntrustment"+">";
         String task="<"+base+"_taskEntrustment"+">";        
              
         ret+=plan+" a oasis:PlanEntrustment.\n";
         ret+=goal+" a oasis:GoalEntrustment.\n";
         ret+=task+" a oasis:TaskEntrustment.\n";  
        
         ret+="<"+this.getAssistantIRI()+">"+" oasis:performsEntrustment "+plan+". \n";
         ret+=plan+" oasis:consistsOfGoalEntrustment "+goal+". \n";
         ret+=goal+" oasis:consistsOfTaskEntrustment "+task+". \n";       
        
         ret+=goal+" oasis:entrustedWith "+goalExecution+" .\n";
         ret+=goal+" oasis:entrustedFrom ?goald .\n";       
         ret+=goal+" oasis:entrustedBy "+"<"+query.get(0).getResource("goal").getURI() +"> .\n";
         ret+=goal+" oasis:entrusts "+" ?agent .\n";
       
         ret+=task+" oasis:entrustedWith "+taskexec+" .\n";
         ret+=task+" oasis:entrustedFrom ?taskd .\n";
         ret+=task+" oasis:entrustedBy "+"<"+query.get(0).getResource("task").getURI() +"> .\n";
         ret+=task+" oasis:entrusts "+" ?agent .\n";    
       }
       else
       {
         ret+=destination+ " oasis:performs "+ taskexec+" .\n";
       }    
       return ret;
    }
    
    
    public String replaceDestination(String q, String d)
    {
      if(d==null)
      {
        return q.replaceAll("//agent//", "?agent");
      }
      return q.replaceAll("//agent//", d);
    }
    
    /**
     * Returns the set of axioms concerning the execution of the given request
     * @param request The ontology of the request     
     * @return the set of axioms representing the execution of the given request
     */
    public ByteArrayOutputStream[] parseRequest(ByteArrayInputStream request)
      {
        String query="";  
        OntologyModel res = null;
        ArrayList<String[]> depends=new ArrayList();
        ArrayList<String[]> configs=null;
       // Stream<OWLAxiom> axioms = Stream.of();
        ByteArrayOutputStream[] toreturn = new ByteArrayOutputStream[]{null,null};
        OWLOntology ontology;
        ontology = this.checkSatelliteData(request);        
        if(ontology==null) //ontology already present in satellite data, otherwise add
        {
            return toreturn;            
        }
        
       // Stream<OWLAxiom> copyOnto=ontology.axioms(); //copy of the request axioms       
        try 
        {
            ontology.imports().forEach(o->{
                ontology.addAxioms(o.axioms());
            });                
            syncReasoner(ontology, null);                
            this.addAxiomsToOntology(this.getDataRequestOntology(), ontology.axioms());            
        } 
        catch (OWLOntologyStorageException ex)
        {
            return toreturn;
        }       
        String IRIrequest = ontology.getOntologyID().getOntologyIRI().get().toString();
        String prefix = getQueryPrefix(IRIrequest);
        query+=prefix;       
        //Analyzing request
        String subquery = prefix + "\n" + this.getQueries().get("Q1.sparql");      
       // System.out.println(subquery);
        try 
        {
          QueryExecution execQ = this.createQuery(ontology, subquery);
          ResultSet resultSet = execQ.execSelect();
          List<QuerySolution> sub11QL=ResultSetFormatter.toList(resultSet);
         //   for(QuerySolution q: sub1QL)
          //      System.out.println(q);
       //   this.getDataBehaviorOntology().importsClosure().forEach(o->ontology.addAxioms(o.axioms()));
          //finding compatible agents
          String destination = null;
          if(sub11QL.get(0).getResource("destination")!=null)
            {
               destination= sub11QL.get(0).getResource("destination").getURI().toString();              
               if(!destination.equals(this.getAssistantIRI()))
                   return toreturn;
               destination ="<"+this.getAssistantIRI()+">";                      
            }
          String execName = sub11QL.get(0).getResource("plan").getLocalName();
          String execNameSpace = sub11QL.get(0).getResource("plan").getNameSpace();
          String goald = "<"+ sub11QL.get(0).getResource("goal").getURI().toString()+">";
          String taskd = "<"+ sub11QL.get(0).getResource("task").getURI().toString()+">";
          Stream<OWLAxiom> axiomToAdd=Stream.empty();
          //customizing the construct header
          String construct=this.replaceDestination(this.getQueries().get("C1.sparql"), destination);         
          String taskexec= "<"+execNameSpace+execName+"_taskExecution"+">";
          String planexec= "<"+execNameSpace+execName+"_planExecution"+">";
          String goalexec= "<"+execNameSpace+execName+"_goalExecution"+">";
          construct=construct.replaceAll("//taskexec//", taskexec)
                             .replaceAll("//taskexeobject//", "<"+execNameSpace+execName+"_exeTaskObject"+">")
                             .replaceAll("//taskexeoperator//", "<"+execNameSpace+execName+"_exeTaskOperator"+">");
                             //.replaceAll("//param1//", "<"+sub11QL.get(0).getResource("plan").getURI()+">");
                             
        
          
          subquery = replaceDestination(this.getQueries().get("Q3.sparql"), destination);
         
          //filtering by task operator matched with the request        
          String theTaskOpElement="<"+sub11QL.get(0).getResource("taskOpElement").getURI()+">";
          subquery=subquery.replaceAll("//thetaskoperator//", theTaskOpElement);
          construct=construct.replaceAll("//param2//", theTaskOpElement);
          String theobject="";
           if(sub11QL.get(0).getResource("referObj").getLocalName().equals("refersExactlyTo"))
              {
                theobject="<"+sub11QL.get(0).getResource("taskObElement").getURI()+">";  
                construct=construct.replaceAll("//theobject//", theobject);
                //here  
              //  System.out.println("testing");
                construct+=getTriplesFromQueryMatch(sub11QL, "<"+sub11QL.get(0).getResource("taskObElement").getURI()+">",
                                                            "obPropType", "taskObElementType");
                //construct+="<"+sub1QL.get(0).getResource("taskObElement").getURI()+"> oasis:hasType ";
               // System.out.println("end testing");
              }
          else //otherwise the request uses the refersAsNewTo for the object reference
               //and then an appropriate element must be sought.
          {
            theobject="?actuator";   
            construct=construct.replaceAll("//theobject//", theobject);
          }
           
          //matching the task argument 
           if(sub11QL.get(0).getResource("taskOpArgElement")!=null)
           {
            List<String> arguments= this.getDistincElemInQuerySet(sub11QL, "taskOpArgElement");           
            for(String s : arguments)
            {
             subquery+= this.getQueries().get("Q4.sparql").replaceAll("//thetaskoperatorarg//", "<"+s+">"); 
             String co= this.getQueries().get("C2.sparql");
             co=co.replaceAll("//taskexec//", taskexec)
               .replaceAll("//taskexeoperatorarg//", "<"+execNameSpace+execName+"_exeTaskOperatorArgument"+">")
               .replaceAll("//param3//", "<"+s+">");
             construct+=co+"\n";
            }
           }
           
          //matching task object 
          subquery+=this.replaceDestination(this.getQueries().get("Q5.sparql"), destination);
          //matching the task objectType
          String obelemType=this.getDistincElemInQuerySetAsString(sub11QL, "taskObElementType");
          
          subquery+=this.getQueries().get("Q6.sparql").replaceAll("//list//", obelemType);
         //if the request uses the refersExacltyTo for the object reference
         //then  the agent must use the element specified in the request 
         
          //inputparameter
          if(sub11QL.get(0).getResource("referInp")!=null)
          {
           String execInpParamElem="";   
           String construct3=this.getQueries().get("C3.sparql");   
           String execInpParam="<"+execNameSpace+execName+"_exeTaskActualInputParameter"+">";
           construct3=construct3.replaceAll("//taskexec//", taskexec);
           construct3=construct3.replaceAll("//taskexeparam//", execInpParam);
           
           if(sub11QL.get(0).getResource("referInp").getLocalName().equals("refersExactlyTo"))
            {
             execInpParamElem="<"+ sub11QL.get(0).getResource("taskInpElement")+">";   
             construct3=construct3.replaceAll("//taskexeparamElem//", execInpParamElem)
                                  .replaceAll("//taskinpprop//", "oasis:refersExactlyTo");
             
           }
          else 
           {
            execInpParamElem="<"+execNameSpace+execName+"_exeTaskActualInputParameterElem"+">";  
            construct3=construct3.replaceAll("//taskexeparamElem//", execInpParamElem)
                                   .replaceAll("//taskinpprop//", "oasis:refersAsNewTo"); 
            //construct2=construct2+this.getQueries().get("C3.sparql").replaceAll("//taskexeparamElem//", execInpParamElem);
            String inpprop=this.getDistincElemInQuerySetAsString(sub11QL, "aInpProp");
           // String inppropElem=this.getDistincElemInQuerySetAsString(sub1QL, "aInpValue");
            String sub5q=this.getQueries().get("Q7.sparql").replaceAll("//list1//", inpprop);    
            
            subquery=subquery+sub5q;
           }
             
           construct3+=getTriplesFromQueryMatch(sub11QL,execInpParamElem,"aInpProp","aInpValue");                     
           construct=construct+construct3;
          }
          construct=createExecEntrustGraph(sub11QL,execNameSpace+execName, planexec, goalexec, taskexec, goald, taskd, destination)+construct;
          //matching agent/actuator configuration
          execQ = this.createQuery(ontology, prefix + this.getQueries().get("Q2.sparql"));
          resultSet = execQ.execSelect();          
          sub11QL=ResultSetFormatter.toList(resultSet); 
          if(sub11QL.size()>0)
          {         
          String subConf=this.replaceDestination(this.getQueries().get("Q9.sparql"), destination).replaceAll("//theobject//",theobject);
          subquery+=subConf;          
          for(int i=0; i<sub11QL.size();i++)
          {
              QuerySolution q=sub11QL.get(i);  
              subquery+="?subConf"+ "<"+q.getResource("confProp").getURI()+">" +" ?cp"+1+".\n";              
          }
           
          }
                  
          //connection information
          subquery+=this.replaceDestination(this.getQueries().get("Q10.sparql"), destination);                
         
          construct+=this.replaceDestination(this.getQueries().get("C5.sparql"), destination);
          subquery = prefix + "CONSTRUCT { "+construct +"}\n" +  subquery + "}"; 
          //System.out.println(subquery);         
         // execQ = this.createQuery(this.getDataBehaviorOntology(), subquery);
        //  resultSet = execQ.execSelect();
        //  List<QuerySolution> sub2QL=ResultSetFormatter.toList(resultSet);           
        //  for(QuerySolution q: sub2QL)
       //   {
       //    System.out.println(q.toString());
       //   }
       
          OWLOntology o= this.getMainManager().createOntology();
          o.addAxioms(this.getDataBehaviorOntology().axioms());
           
          OntologyModel m =  performQuery(o, subquery);
               
          OWLOntology out = getMainManager().createOntology(m.axioms());
       //   OWLOntology out = getMainManager().createOntology();
          toreturn[0]=new ByteArrayOutputStream();
          out.saveOntology(toreturn[0]);       
          addAxiomsToOntology(this.getDataBeliefOntology(), out.axioms());
          
          this.getMainManager().removeOntology(out);
          
          
        //  String filesource = this.getOntologiesDevicesPath() + File.separator + "bbbbbbb" + ".owl";
        //  File file = new File(filesource);
       //   this.getMainManager().saveOntology(o, IRI.create(new File( "bbbbb" + ".owl")));
            
          
          
        } 
        catch (OWLOntologyCreationException ex)
        {
           return null;
        } 
        catch (OWLOntologyStorageException ex)
        {
           return null;
        }
        return toreturn;
      }
    
    /**
     * Retrieve the given data from the belief and request ontologies
     * @param iriInd the data to be retrieved 
     * @return the axioms of the found data
     */
    public Stream<OWLAxiom> retrieveBeliefAssertions(String iriInd)
      {
         
         return retrieveAssertions(iriInd, Stream.concat(getDataRequestOntology().axioms(), 
                                                         getDataBeliefOntology().axioms()));
      }
    
    /**
     * Retrieve the given data from the belief ontology
     * @param input the data to find as InpuStream
     * @return the axioms of the found data
     * @throws OWLOntologyCreationException
     */
    public Stream<OWLAxiom> retrieveDataBelief(InputStream input) throws OWLOntologyCreationException
    {
      OWLOntology ontology=this.secureLoadOntology(input);
      if(ontology==null)
          return null;
      Stream<OWLAxiom> axioms=Stream.empty();
      Iterator<OWLNamedIndividual> iterator=ontology.individualsInSignature().iterator();
      while(iterator.hasNext())
      { 
        OWLNamedIndividual individual=iterator.next();
              axioms=Stream.concat(this.getDataBeliefOntology().axioms().filter(x->x.isLogicalAxiom())
                                              .filter(x->x.isOfType(AxiomType.OBJECT_PROPERTY_ASSERTION) || x.isOfType(AxiomType.DATA_PROPERTY_ASSERTION))
                                              .filter(x->x.containsEntityInSignature( individual))
                    ,axioms);
      }
      this.getMainManager().removeOntology(ontology);
      return axioms;
    }
    
    
    
      //retrieve the assertion concerning the given individual as string    
    private Stream<OWLAxiom> retrieveAssertionsWithNewIndividual(String iriInd, String newIndividual, Stream<OWLAxiom> ontology)
      {         
        try
          {
            OWLOntology o=this.getMainManager().createOntology(ontology);
            final OWLEntityRenamer renamer = new OWLEntityRenamer(this.getMainManager(), Collections.singleton(o));
            final Map<OWLEntity, IRI> entity2IRIMap = new HashMap<>();
            o.individualsInSignature().forEach(toRename ->
                                      {
                                        final IRI iri = toRename.getIRI();
                                        if(iri.getIRIString().equals(iriInd))
                                          {
                                            entity2IRIMap.put(toRename, IRI.create(iri.toString().replace(iriInd, newIndividual)));                                            
                                          }
                                      });
            o.applyChanges(renamer.changeIRI(entity2IRIMap));   
            //o.axioms().forEach(System.out::println);
            Stream<OWLAxiom> a= o.axioms();
            this.getMainManager().removeOntology(o);            
            return a;        
          }
        catch (OWLOntologyCreationException ex)
          {
           getLogger().log(Level.SEVERE, null, ex);
            return null;
          }
      }
    
     //retrieve the assertion concerning the given individual as string    
    private Stream<OWLAxiom> retrieveAssertions(String iriInd, Stream<OWLAxiom> ontology)
      {     
         
         OWLNamedIndividual individual=this.getMainManager().getOWLDataFactory().getOWLNamedIndividual(iriInd);         
         Stream<OWLAxiom> axioms=ontology.filter(x->x.isLogicalAxiom())
                                              //.filter(x->x.isOfType(AxiomType.OBJECT_PROPERTY_ASSERTION) || x.isOfType(AxiomType.DATA_PROPERTY_ASSERTION))
                                              .filter(x->x.containsEntityInSignature(individual));
         return axioms;        
      }
      
    //retrieve the assertion concerning the object of the OASIS property refersTo   
     private Stream<OWLAxiom> retrieveRefersToAssertions(String iriInd, Stream<OWLAxiom> ontology)
      {     
         String prop=this.getMainOntology().getOntologyID().getOntologyIRI().get().toString()+"#refersTo";
         OWLNamedIndividual individual=this.getMainManager().getOWLDataFactory().getOWLNamedIndividual(iriInd);         
         Stream<OWLAxiom> axioms=ontology.filter( x-> ( ( (x.individualsInSignature().filter(val-> val.toStringID().equals(iriInd)).count()) > 0) &&
                                                           (( x.isOfType(AxiomType.OBJECT_PROPERTY_ASSERTION) &&
                                                           !((OWLObjectPropertyAssertionAxiom) x).getProperty().asOWLObjectProperty().toStringID().equals(prop))
                                                             || x.isOfType(AxiomType.DATA_PROPERTY_ASSERTION))));                  
        return axioms;        
      }
    
    /**
     * Set the execution status of the given execution as the given status value
     * @param execution the execution to be updated
     * @param status the new status
     * @throws OWLOntologyStorageException
     */
    public void setExecutionStatus (String execution, String status) throws OWLOntologyStorageException
      {
        OWLNamedIndividual individual=this.getMainManager().getOWLDataFactory().getOWLNamedIndividual(execution);
        OWLNamedIndividual indstatus=this.getMainManager().getOWLDataFactory().getOWLNamedIndividual(this.getMainOntology().getOntologyID().getOntologyIRI().get().toString()+"#"+status);
        OWLObjectProperty property=this.getMainManager().getOWLDataFactory().getOWLObjectProperty(this.getMainOntology().getOntologyID().getOntologyIRI().get().toString()+"#hasStatus");
        this.getDataRequestOntology().addAxiom(this.getMainManager().getOWLDataFactory().getOWLObjectPropertyAssertionAxiom(
                property, individual, indstatus));
        this.getMainManager().saveOntology(this.getDataRequestOntology());      
      }

    /**
     * Return path and file name of the given ontology as IRI
     * @param iri the iri of the ontology
     * @return the path and file name as String[]
     */
    public String[] getPathAndName(String iri)
      {
        
        OWLOntology ontology=this.getMainManager().getOntology(IRI.create(iri));
        if(ontology==null)
            return null;
        List<IRI> iris= new ArrayList<>();
        this.getMainManager().getIRIMappers().forEach(a->{
                                                           IRI d=a.getDocumentIRI(ontology.getOntologyID().getOntologyIRI().get());
                                                           if(d!=null)
                                                               iris.add(d); 
        });
        if (iris.isEmpty())
            return null;  
        //System.out.println(iris.toString());      
        return new String[]{iris.get(0).toString(),iris.get(0).getShortForm()};
      }
    
    /**
     * Update the connection information of the given devices as IRI
     * @param iri the IRI of the device to be updated
     * @param address the new address
     * @param port the new port number
     * @return return 1 if the device as been correctly updated, 0 otherwise, -1 if some errors occur
     */
    public int modifyConnection(String iri, String address, String port) //to do
      {
         String ontoInfo[]=getPathAndName(iri);
         if(ontoInfo==null)
             return 0;
         
         OWLOntology ontology;
         ontology=restoreFromSource(iri, this.getBackupPath(), ontoInfo[1]);
         if( ontology==null)
            return 0;             
                  
         String portProp=this.getMainOntology().getOntologyID().getOntologyIRI().get().toString()+"#hasPortNumber";
         String addrProp=this.getMainOntology().getOntologyID().getOntologyIRI().get().toString()+"#hasIPAddress";
         String connInf=this.getMainOntology().getOntologyID().getOntologyIRI().get().toString()+"#hasConnectionInfo";
                
         List<OWLNamedIndividual> conn= new ArrayList<>();
         List<OWLAxiom> assertions= new ArrayList<>();
         ontology.axioms().filter(x->x.isOfType(AxiomType.DATA_PROPERTY_ASSERTION)).forEach( ax->
                          {                             
                            OWLDataPropertyAssertionAxiom cl= (OWLDataPropertyAssertionAxiom) ax;
                            if(cl.getProperty().asOWLDataProperty().toStringID().equals(portProp) 
                                    || (cl.getProperty().asOWLDataProperty().toStringID().equals(addrProp))
                                    || (cl.getProperty().asOWLDataProperty().toStringID().equals(connInf)))
                              {
                                conn.add(cl.getSubject().asOWLNamedIndividual());
                                assertions.add(cl);                                
                              }                           
                          });
         
         
        if(conn.isEmpty() || assertions.isEmpty())
            return 0;
        
        assertions.forEach(x-> {  ontology.removeAxiom(x); });
        
        ontology.addAxiom(this.getMainManager().getOWLDataFactory()
                             .getOWLDataPropertyAssertionAxiom(
                                     this.getMainManager().getOWLDataFactory().getOWLDataProperty(IRI.create(portProp)),
                                     conn.get(0), 
                                      this.getMainManager().getOWLDataFactory().getOWLLiteral(port, OWL2Datatype.XSD_INT)));
        
        ontology.addAxiom(this.getMainManager().getOWLDataFactory()
                             .getOWLDataPropertyAssertionAxiom(
                                     this.getMainManager().getOWLDataFactory().getOWLDataProperty(IRI.create(addrProp)),
                                     conn.get(0), 
                                      this.getMainManager().getOWLDataFactory().getOWLLiteral(address)));                                        
               
              
        updateFromPath(ontoInfo[1], this.getBackupPath(), ontology);
        
        try
          { 
            String thef=ontoInfo[0];
            thef=thef.substring(6,thef.length());            
            syncReasoner(ontology, thef);
            this.getMainManager().saveOntology(ontology, IRI.create(ontoInfo[0]));
          } 
        catch (OWLOntologyStorageException ex)
          {            
            return -1;
          }
        return 1;                             
         
      }
    
    /**
     * Update the given ontology as IRI and depending on the boolean updateBackup, update also the backup file, and depending on the oolean delete
     * remove the original file
     * @param iri the ontology to be updated
     * @param updateBackup 
     * @param delete
     * @return the IRI of the updated ontology
     */
    public String updateOntology(String iri, boolean updateBackup, boolean delete) 
      {
         String ontoInfo[]=getPathAndName(iri);
         if(ontoInfo==null)
             return null;
        try
          { 
            OWLOntology ontology=restoreFromSource(iri, this.getSatellitePath(), ontoInfo[1]);
            if( ontology==null)
             return null;
                                  
            if(updateBackup)
               this.getMainManager().saveOntology(ontology, IRI.create(new File(this.getBackupPath()+File.separator+ontoInfo[1])));
            String thef=ontoInfo[0];
            thef=thef.substring(6,thef.length());            
            syncReasoner(ontology, thef);
            this.getMainManager().saveOntology(ontology, IRI.create(ontoInfo[0]));
            if(delete)
              {
                File todelete=new File(this.getSatellitePath()+File.separator+ontoInfo[1]);
                todelete.delete();
                this.getMainManager().getIRIMappers().remove(new SimpleIRIMapper(ontology.getOntologyID().getOntologyIRI().get(),
                IRI.create(todelete.getCanonicalFile())));
              } 
          } 
        catch (OWLOntologyStorageException  | IOException ex)
          {            
            return null;
          }
        return iri;   
      }
     
    //restore the given ontology from the given path in the given file name
    private OWLOntology restoreFromSource(String iri, Path path, String name )           
      {
        OWLOntology ontology=this.getMainManager().getOntology(IRI.create(iri));
        if(ontology==null)
            return null;
        List<IRI> iris= new ArrayList<>();
        this.getMainManager().getIRIMappers().forEach(a->{
                                                           IRI d=a.getDocumentIRI(ontology.getOntologyID().getOntologyIRI().get());
                                                           if(d!=null)
                                                               iris.add(d);
        }); 
        try
          {     
            File f=new File(path + File.separator +name);
            if (!f.exists())
                return null;
            this.getMainManager().removeOntology(ontology); 
            this.getMainManager().getIRIMappers().remove(new SimpleIRIMapper(ontology.getOntologyID().getOntologyIRI().get(),
                IRI.create(f.getCanonicalFile())));
            OWLOntology g=this.getMainManager().loadOntologyFromOntologyDocument(f);
            g.saveOntology(IRI.create(f));
            return g;            
          } 
        catch (OWLOntologyCreationException |  OWLOntologyStorageException | IOException ex)
          {            
            return null;
          }        
      }
    
     //save the ontology in the current path with the given name
      private int updateFromPath(String name, Path path, OWLOntology ontology)
      {        
        try
          {
            this.getMainManager().saveOntology(ontology, IRI.create(new File(path + File.separator + name)));
          } 
        catch (OWLOntologyStorageException ex)
          {
            return 0;
          }
        return 1;
      }

    /**
     * Update the ontology of the given device
     * @param description
     * @return the iri of the updated device
     */
    public String modifyDevice(String description)
        {
           String s=this.updateOntology(description, true, true);
           getSatellite().remove(description);           
           return s;
        }
}

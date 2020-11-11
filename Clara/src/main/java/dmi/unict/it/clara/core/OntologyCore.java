/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dmi.unict.it.clara.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.Stream;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.ChangeApplied;
import org.semanticweb.owlapi.model.parameters.OntologyCopy;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import ru.avicomp.ontapi.OntManagers;
import ru.avicomp.ontapi.OntologyManager;
import ru.avicomp.ontapi.OntologyModel;




/**
 *
 * @author Daniele Francesco Santamaria
 */
public class OntologyCore
  {
    private final OWLOntologyManager manager; 
    private final OWLDataFactory datafactory;
    private OWLOntology mainOntology;
    private Model graphmodel;
    private OWLReasoner reasoner;
    private OWLOntologyLoaderConfiguration config;
    private Logger logSys;
    private final int limit = 1024*10000; //10 MB
    /**
     * Returns the main datafactory
     * @return the main datafactory
     */
    public OWLDataFactory getDataFactory()
      {
         return this.datafactory;
      }
    
    /**
     * Returns the main ontology
     * @return the main ontology
     */
    public OWLOntology getMainOntology()
      {
         return this.mainOntology;
      }
    
    /**
     * Returns the main ontology manager
     * @return the main ontology manager
     */
    public OWLOntologyManager getMainManager()
      {
        return this.manager;
      }
    
    /**
     * Sets the local grap hmodel 
     * @param model the input model
     */
    public void setGraphModel(Model model)
      {
        graphmodel=model;
      }
    
    /**
     * Returns the local graph model.
     * @return the local graph model
     */
    public Model getGraphModel()
      {
        return graphmodel;
      }
    
    /**
     * Returns the owl reasoner
     * @return the owl reasoner
     */
    public OWLReasoner getReasoner()
      {
         return reasoner;
      }
    
    /**
     * Sets the owl reasoner with the given one
     * @param owlreasoner the owl reasoner
     */
    public void setReasoner(OWLReasoner owlreasoner)
      {
        reasoner=owlreasoner;
      }
    
    
    public Logger getLogger()
    {
      return this.logSys;
    }
    
    
    /**
     *   Constructs an empty OntologyCore object with empty ontology
     */
    public OntologyCore (String logName)
      {
        try {//
            logSys=Logger.getLogger(logName);
            FileHandler file = new FileHandler(logName+".log", limit,1 , true);  
            file.setLevel(Level.ALL);
            SimpleFormatter simple = new SimpleFormatter();
            file.setFormatter(simple);
            logSys.addHandler(file);
            logSys.setLevel(Level.ALL);           
            logSys.setUseParentHandlers(false);
        } 
        catch (IOException | SecurityException ex)
        {            
            System.err.println("Logger cannot be created");
        } 
         manager = OWLManager.createOWLOntologyManager(); //create the manager   OWLOntologyLoaderConfiguration config = new OWLOntologyLoaderConfiguration();
         config = new OWLOntologyLoaderConfiguration();
         manager.setOntologyLoaderConfiguration(config);
         datafactory = manager.getOWLDataFactory();
         mainOntology=null;
         graphmodel=null;         
      }
    
    /**
     * Assigns the main ontology to the OntologyCore object
     * @param inputFile
     * @throws OWLOntologyCreationException
     */
    public void setMainOntology(File inputFile) throws OWLOntologyCreationException 
      {        
        mainOntology=manager.loadOntologyFromOntologyDocument(inputFile);
      } 
    
    /**
     * Merges  the main ontology with a given file representing an ontology
     * @param mergeFile The file to be merged
     * @return true if the merge has been successifully done
     */    
    public boolean addAxioms(File mergeFile)
      {
        OWLOntologyManager managerMerge=OWLManager.createOWLOntologyManager();         
        try
          {
            OWLOntology mergeOntology= managerMerge.loadOntologyFromOntologyDocument(mergeFile);
            return addAxioms(mergeOntology);
          } 
        catch (OWLOntologyCreationException ex)
          {
            Logger.getLogger(OntologyCore.class.getName()).log(Level.SEVERE, null, ex);
            return false;
          }
        
      }
    
    public Stream<OWLAxiom> getAllAxioms()
      {
            Stream<OWLAxiom> axs= Stream.empty();
            this.getMainManager().ontologies().forEach( ont ->
              {
                Stream.concat(axs,ont.axioms());
              });
            return axs;
      }
    
    /**
     * Merges the main ontology with a given  ontology
     * @param mergeOntology the ontology to be merged
     * @return true if the merge has been successifully done
     */
    public boolean addAxioms(OWLOntology mergeOntology)
      {
          ChangeApplied changes=getMainOntology().addAxioms(mergeOntology.axioms());
          return changes== ChangeApplied.SUCCESSFULLY;
      }
    
    
    /**
     * Deletes from the main ontology an  ontology represented by the given file
     * @param removeFile the ontology to be removed
     * @return true if the delete has been successifully done
     */
    public boolean removeAxioms(File removeFile)
      {
        OWLOntologyManager managerMerge=OWLManager.createOWLOntologyManager();         
        try
          {
            OWLOntology removeOntology= managerMerge.loadOntologyFromOntologyDocument(removeFile);
            return removeAxioms(removeOntology);
          } 
        catch (OWLOntologyCreationException ex)
          {
            Logger.getLogger(OntologyCore.class.getName()).log(Level.SEVERE, null, ex);
            return false;
          }
        
      }
    
    /**
     * Removes from the main ontology a given  ontology
     * @param removeOntology the ontology to be removed
     * @return true if the delete has been successifully done
     */
    public boolean removeAxioms(OWLOntology removeOntology)
      {
          ChangeApplied changes=getMainOntology().remove(removeOntology.axioms());
          return changes== ChangeApplied.SUCCESSFULLY;
      }
    
     
     /**
     *  Creates a QueryExecution object from the given ontology and query as string
     * @param ontology the ontology to be queried
     * @param query  the string representing the query
     * @return the QueryExecution object which performs the query
     * @throws org.semanticweb.owlapi.model.OWLOntologyCreationException
     */
    public QueryExecution createQuery(OWLOntology ontology, String query) throws OWLOntologyCreationException
      {
//         OntologyModel ontologyModel=((OntologyModel) ontology);
//         Model m=ontologyModel.asGraphModel();      
         
         OntologyManager ontManager = OntManagers.createONT();
         OntologyModel ontOntology;           
         ontOntology = ontManager.copyOntology(ontology, OntologyCopy.DEEP);          
         Model m = ontOntology.asGraphModel();
         this.setGraphModel(m);         
         QueryExecution qexec = QueryExecutionFactory.create(QueryFactory.create(query), this.getGraphModel());
         return qexec;
      }
    
    /**
     * Performs a Selet query over the given QueryExecution object
     * @param qexec the QueryExecution object 
     * @return the ResultSet object representing the query result
     */
    public ResultSet performSelectQuery(QueryExecution qexec)
      {        
        ResultSet res  = qexec.execSelect();    
        return res;
      }
   
    /**
      * Performs a Construct query over the given QueryExecution object
     * @param qexec the QueryExecution object 
     * @return the ResultSet object representing the query result
     * @throws java.io.IOException
     * @throws org.semanticweb.owlapi.model.OWLOntologyCreationException
     */
    public OntologyModel performConstructQuery(QueryExecution qexec) throws IOException, OWLOntologyCreationException
      {        
        Model res  =  qexec.execConstruct();        
        ByteArrayOutputStream out=new ByteArrayOutputStream();        
        res.write(out,"ttl");
        OntologyManager omanager = OntManagers.createONT();
        OntologyModel ontology = omanager.loadOntologyFromOntologyDocument(new ByteArrayInputStream(out.toByteArray()));
        return ontology;              
      }
    
  
    
    
    public OWLNamedIndividual createIndividualInBase(String indIriBase, OWLClass owlclass, OWLOntology ontology, OWLDataFactory datafactory) {
        OWLNamedIndividual individual = datafactory.getOWLNamedIndividual(indIriBase);
        ontology.addAxiom(datafactory.getOWLClassAssertionAxiom(owlclass, individual));
        return individual;
    }

    public OWLNamedIndividual createIndividualInBase(String indIriBase, String classIriBase, OWLOntology ontology, OWLDataFactory datafactory) {
        OWLNamedIndividual individual = datafactory.getOWLNamedIndividual(indIriBase);
        ontology.addAxiom(datafactory.getOWLClassAssertionAxiom(datafactory.getOWLClass(classIriBase), individual));
        return individual;
    }

    public void createObjectPropertyAssertionAxiom(String property, OWLNamedIndividual subject, OWLNamedIndividual object, OWLOntology ontology, OWLDataFactory datafactory) {
        ontology.addAxiom(datafactory.getOWLObjectPropertyAssertionAxiom(
                datafactory.getOWLObjectProperty(property), subject, object));
    }

    public void createDataPropertyAssertionAxiom(String property, OWLNamedIndividual individual, String value, OWL2Datatype datatype, OWLOntology ontology, OWLDataFactory datafactory) {
        ontology.addAxiom(datafactory.getOWLDataPropertyAssertionAxiom(
                datafactory.getOWLDataProperty(property), individual,
                datafactory.getOWLLiteral(value, datatype)));
    }
    
 }

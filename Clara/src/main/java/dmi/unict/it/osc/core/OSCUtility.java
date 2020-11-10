/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dmi.unict.it.osc.core;

import java.math.BigInteger;
import io.ipfs.api.IPFS;
import io.ipfs.api.MerkleNode;
import io.ipfs.api.NamedStreamable;
import io.ipfs.multiaddr.MultiAddress;
import io.ipfs.multihash.Multihash;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.DatatypeConverter;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthEstimateGas;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tuples.generated.Tuple3;
import org.web3j.tuples.generated.Tuple7;
import org.web3j.tx.RawTransactionManager;
import org.web3j.utils.Numeric;
/**
 *
 * @author Daniele Francesco Santamaria
 */
public class OSCUtility
  {
    private  IPFS ipfs; 
    private  Web3j web3;
    private  Credentials credentials; 
    private Oasisosc contract;
    private String defaultaddress;
    private RawTransactionManager txManager;
  //  protected BigDecimal currentGasPrice=new BigDecimal(250); //2Gwei for gas unit
    
    public OSCUtility(String ipfsaddress, String ethereumAddr, String privateKey) throws OSCUtilityConnectionException
    {
     try
       {
        ipfs = new IPFS(new MultiAddress(ipfsaddress));    
        System.out.println("Connected to IPFS at "+ipfsaddress);
        web3 = Web3j.build(new HttpService(ethereumAddr));
        System.out.println("Connected to Ethereum node: "+web3.web3ClientVersion().send().getWeb3ClientVersion());
        credentials=Credentials.create(privateKey);   
        txManager=new RawTransactionManager(web3, credentials);
        System.out.println("Welcome address: "+credentials.getAddress());
       }    
     catch (IOException e )
       {
         this.handleStartUpException();
         throw new OSCUtilityConnectionException("Cannot correctly create OSCUtility. Couldn't connect to Ethereum node or invalid credentials.");
       } 
     catch(Exception e)
       {
         this.handleStartUpException();
         throw new OSCUtilityConnectionException("Cannot correctly create OSCUtility. Couldn't connect to IPFS or invalid Ethereum credentials.");
       }
    } 

    public RawTransactionManager getTransactionManager()
    {
        return this.txManager;
    }
    
    private void handleStartUpException()
      {
        this.ipfs=null;
        this. web3=null;
        this.credentials=null; 
      }

   public Web3j getWeb3jClient()
     {
      return this.web3;
     }
     
   public IPFS getIPFSClient()
     {
       return this.ipfs;
     }
   
   public Credentials getCredentials()
     {
       return this.credentials;
     }
   
  /**
     * This method deploys the smart contract.
     * @param name The name of the token
     * @param symbol The symbol of the token
     * @return the transaction receipt
    **/
    public TransactionReceipt __deploy__(String name, String symbol)
    {       
        try
        {            
            this.setContract(Oasisosc.deploy(this.getWeb3jClient(), this.getCredentials(), new CustomGasProvider(new BigDecimal(this.getCurrentGasPrice()), this.getCurrentGasLimit()), name, symbol).send());            
            this.setAddress(this.getContract().getContractAddress());          
            return this.getContract().getTransactionReceipt().get();
        } 
        catch (Exception ex)
        {
            this.setContract(null);
            Logger.getLogger(Oasisosc.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }  
    /**
     * Return the object representing the smart contract
     * @return the object representing the smart contract
     */
    public Oasisosc getContract()
    {
      return contract;
    }
    
   public String getDefaultAddress()
    {
        return defaultaddress;
    }
    
    public void setAddress(String _address)
    {
      defaultaddress=_address;
    }
      
    public BigInteger getCurrentGasLimit() throws IOException
    {
      return web3.ethGetBlockByNumber(DefaultBlockParameter.valueOf("latest"),true).send().getBlock().getGasLimit();
    }
    
    public void setContract(Oasisosc _contract)
    {
      contract=_contract;
      defaultaddress =_contract.getContractAddress();
    }
    
    
        /**
     * Return the value of the current gas price
     * @return a BigInteger representing the current gasPrice, 0 if an error occurs
     */
    public BigInteger getCurrentGasPrice()
    {
        try
        {            
           return this.getWeb3jClient().ethGasPrice().send().getGasPrice();
        } 
        catch (IOException ex)
       {
            Logger.getLogger(OSCUtility.class.getName()).log(Level.SEVERE, null, ex);
            return null;
       }
    }
    
    
       /**
     * Load the smart contract from its address
     * @param address the address of the smart contract              
     */
    public void load (String address)
        {                   
        
        try {
            contract=Oasisosc.load(address, web3, credentials, new CustomGasProvider(new BigDecimal(this.getCurrentGasPrice()),this.getCurrentGasLimit()));
            this.setAddress(contract.getContractAddress());
            // applyFilter();
        } 
        catch (IOException ex)
        {
            Logger.getLogger(Oasisosc.class.getName()).log(Level.SEVERE, null, ex);
            contract=null;
        }
        }  
    
    public BigInteger getTransactionEstimation(String from, String data) throws IOException
    {                    
            Transaction t = Transaction.createEthCallTransaction(from, this.getDefaultAddress(), data);
            EthEstimateGas estim = this.getWeb3jClient().ethEstimateGas(t).send();
            BigInteger amount = estim.getAmountUsed();           
            BigInteger perc= amount.multiply(BigInteger.valueOf(20));
            perc=perc.divide(BigInteger.valueOf(100));            
            amount = amount.add(perc);
            return amount;        
    }
    
    
    public String mint(String ontology, String query, BigInteger previous)
      {
        try      
          { 
            NamedStreamable.ByteArrayWrapper ontologyIPFS = new NamedStreamable.ByteArrayWrapper(ontology.getBytes());  
            NamedStreamable.ByteArrayWrapper queryIPFS = new NamedStreamable.ByteArrayWrapper(query.getBytes());  
            MerkleNode ontologyMN = ipfs.add(ontologyIPFS).get(0);
            MerkleNode queryMN = ipfs.add(queryIPFS).get(0);           
            String ontologyEX=DatatypeConverter.printHexBinary(ontologyMN.hash.toBytes());
            String queryEX=DatatypeConverter.printHexBinary(queryMN.hash.toBytes());
            byte[] ontologyDigest = Numeric.hexStringToByteArray(ontologyEX.substring(4, ontologyEX.length()));
            byte[] queryDigest = Numeric.hexStringToByteArray(queryEX.substring(4, ontologyEX.length()));    
            //this.getContract().setGasProvider(new CustomGasProvider(new BigDecimal(this.getCurrentGasPrice()), this.getCurrentGasLimit()));
            //TransactionReceipt rec=
             String data =  this.getContract().mint(new BigInteger(ontologyEX.substring(0, 2)), new BigInteger(ontologyEX.substring(2, 4)), ontologyDigest,
                                                                  new BigInteger(queryEX.substring(0, 2)),  new BigInteger(queryEX.substring(2, 4)), queryDigest, 
                                                                  previous).encodeFunctionCall();            
             BigInteger limit=getTransactionEstimation(this.getCredentials().getAddress(), data);
             String rec= this.getTransactionManager().sendTransaction(this.getCurrentGasPrice(), 
                                                                                  limit,                                                                                   
                                                                                  this.getDefaultAddress(), 
                                                                                  data, BigInteger.ZERO).getTransactionHash();
            return rec;
          } 
          catch (IOException ex)
          {
              System.out.println(ex.toString());
          } 
         catch (Exception ex)
          {
            System.out.println(ex.toString());
          }
          return null;
      } 
    
    public String computeIPFSCIDFromMultiHash( BigInteger f1, BigInteger f2, byte[] f3)
      {         
        String ipfscid=f1+""+f2+ DatatypeConverter.printHexBinary(f3);
        return ipfscid;
      }
    
       
    public boolean tokenIDExists(BigInteger id) throws Exception
    {
     return this.getContract().tokenIDExists(id).send();
    }
    
    
    public boolean tokenExists(String ontology, String query, BigInteger previous) 
    {
        try {
         
            NamedStreamable.ByteArrayWrapper ontologyIPFS = new NamedStreamable.ByteArrayWrapper(ontology.getBytes());  
            NamedStreamable.ByteArrayWrapper queryIPFS = new NamedStreamable.ByteArrayWrapper(query.getBytes());  
            MerkleNode ontologyMN = ipfs.add(ontologyIPFS).get(0);
            MerkleNode queryMN = ipfs.add(queryIPFS).get(0);           
            String ontologyEX=DatatypeConverter.printHexBinary(ontologyMN.hash.toBytes());
            String queryEX=DatatypeConverter.printHexBinary(queryMN.hash.toBytes());
            byte[] ontologyDigest = Numeric.hexStringToByteArray(ontologyEX.substring(4, ontologyEX.length()));
            byte[] queryDigest = Numeric.hexStringToByteArray(queryEX.substring(4, ontologyEX.length()));    
            
          return this.getContract().tokenExists(new BigInteger(ontologyEX.substring(0, 2)), new BigInteger(ontologyEX.substring(2, 4)), ontologyDigest,
                                                                  new BigInteger(queryEX.substring(0, 2)),  new BigInteger(queryEX.substring(2, 4)), queryDigest, 
                                                                  previous).send();            
            
        } 
        catch (Exception ex) {
           System.out.println(ex);
           return false;
        }
    }
    
    /**
     * Destroy (burn) a token
     * @param id the ID of the t token
     * @return the transaction receipt
     */
    public String burn(BigInteger id)
    {
        try
        {
          String data =this.getContract().burn(id).encodeFunctionCall();
          BigInteger limit=getTransactionEstimation(this.getCredentials().getAddress(), data);
          String rec= this.getTransactionManager().sendTransaction(this.getCurrentGasPrice(), 
                                                                                  limit,                                                                                   
                                                                                  this.getDefaultAddress(), 
                                                                                  data, BigInteger.ZERO).getTransactionHash();
            return rec;
          
        } 
        catch (Exception ex)
        {
            Logger.getLogger(Oasisosc.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    } 
    
    
    public String[] getTokenInfo(BigInteger id) throws Exception
    {
      Tuple7<BigInteger, BigInteger, byte[], BigInteger, BigInteger, byte[], BigInteger> rec = this.getContract().getTokenInfo(id).send();
      String ont=computeIPFSCIDFromMultiHash(rec.component1(), rec.component2(), rec.component3());
      String query = computeIPFSCIDFromMultiHash(rec.component4(), rec.component5(), rec.component6());
      String[] ret= new String[]{Multihash.fromHex(ont).toString(),
                                 Multihash.fromHex(query).toString(), 
                                 rec.component7().toString()};       
      return ret;
    }
    
    private String readFromIPFSCID(String ipfscid)
      {  
       
        try
          {
            Multihash multihash = Multihash.fromHex(ipfscid);               
            byte[] content;
            content = ipfs.cat(multihash);
            return new String(content);
          } 
        catch (IOException ex)
          {
            System.out.println(ex.toString());
            return null;
          }        
      }

    public String getTokenOwner(BigInteger id) throws Exception
     {
       return this.getContract().ownerOf(id).send();
     }
    
    public String transferToken(String from, String to, BigInteger id) throws Exception
    {
      //this.getContract().setGasProvider(new CustomGasProvider( new BigDecimal(this.getCurrentGasPrice()), this.getCurrentGasLimit()));  
      String data = this.getContract().transferFrom(from, to, id).encodeFunctionCall();
      BigInteger limit=getTransactionEstimation(this.getCredentials().getAddress(), data);
       String rec= this.getTransactionManager().sendTransaction(this.getCurrentGasPrice(), 
                                                                                  limit,                                                                                   
                                                                                  this.getDefaultAddress(), 
                                                                                  data, BigInteger.ZERO).getTransactionHash();
       return rec;
    }
    
    public BigInteger getBalance(String address)
     {
        try {
            return this.getContract().balanceOf(address).send();
        } catch (Exception ex) {
            Logger.getLogger(OSCUtility.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
     }
  }

 
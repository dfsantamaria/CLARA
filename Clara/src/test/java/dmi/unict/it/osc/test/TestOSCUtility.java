package dmi.unict.it.osc.test;

import dmi.unict.it.osc.core.OSCUtility;
import dmi.unict.it.osc.core.Oasisosc;
import io.ipfs.api.IPFS;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;



/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author Daniele Francesco Santamaria
 */
public class TestOSCUtility
  {
    private static IPFS ipfs; 
    private static Web3j web3;
    private static Credentials credentials;  
    
    public static void main (String[] args)
      {
        String ipfsaddress="/ip4/127.0.0.1/tcp/5001";
        String ethereumAddr="HTTP://127.0.0.1:7545";
        String privateKey="b0cf7579e4ff2dfefdd62b515426eed6510058dfcb0ebf442182365602a40f05";
        String address="0x8eC430d99BeB8270Da3604b839AE7E3477DEAc35";
       try
       {
        OSCUtility oscutil=new OSCUtility(ipfsaddress, ethereumAddr, privateKey);
        oscutil.__deploy__("OasisOSC","OSC"); 
        System.out.println(oscutil.getDefaultAddress());
       // oscutil.load("0xa55f20e233427493007b73d8f7e2ec66bd9ddab8");
        String ontcontent=new String (Files.readAllBytes(Paths.get("E:\\Dropbox\\Project\\CLARA\\papers\\Proposal\\Ontology\\oasis.OWL")));
        String querycontent=new String (Files.readAllBytes(Paths.get("D:\\danie\\OneDrive\\Documenti\\NetBeansProjects\\EthereumTokenOSC\\EthereumTokenOSC\\EthereumTokenOSC\\src\\main\\dat\\sparql.sp")));
        
       
//        
//        TransactionReceipt rec=oscutil.mint(ontcontent, querycontent, BigInteger.valueOf(0));
//        List<Oasisosc.TokenMintedEventResponse> list=oscutil.getContract().getTokenMintedEvents(rec);
//        for (Oasisosc.TokenMintedEventResponse l : list)
//        {
//            System.out.println(l.tokenId);
//        }
//                         
//        System.out.println(oscutil.tokenIDExists(BigInteger.ONE));
//        System.out.println(oscutil.tokenExists(ontcontent, querycontent, BigInteger.ONE));
//        System.out.println(oscutil.getTokenOwner(BigInteger.ONE)); 
//        String[] tok=oscutil.getTokenInfo(BigInteger.ONE);
//        System.out.println(tok[0]+" "+tok[1]+" "+tok[2]);
//         
//        
//        System.out.println("transfering");
//        
//       // oscutil.transferToken(address, "0x24282682c9db80eC20E53EF9A027B2689b72b25c", BigInteger.ONE);
//       // System.out.println(oscutil.getTokenOwner(BigInteger.ONE)); 
//        
//        
//        System.out.println("burning");
//        rec=oscutil.burn(BigInteger.ONE);
//        List<Oasisosc.TokenBurnedEventResponse> b=oscutil.getContract().getTokenBurnedEvents(rec);
//        for (Oasisosc.TokenBurnedEventResponse l : b)
//        {
//            System.out.println(l.tokenId);
//        }
//                 
//         System.out.println(oscutil.tokenIDExists(BigInteger.ONE));
//         System.out.println(oscutil.tokenExists(ontcontent, querycontent, BigInteger.ONE));
//         tok=oscutil.getTokenInfo(BigInteger.ONE);
//         System.out.println(tok[0]+" "+tok[1]+" "+tok[2]);
//         System.out.println(oscutil.getTokenOwner(BigInteger.ONE));
       }          
     catch(Exception e)
       {        
         System.out.println(e.toString());
         System.exit(1);
       }
    }         
         
  }
  

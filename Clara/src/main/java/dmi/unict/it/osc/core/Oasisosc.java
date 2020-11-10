package dmi.unict.it.osc.core;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple7;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 4.5.16.
 */
@SuppressWarnings("rawtypes")
public class Oasisosc extends Contract {
    public static final String BINARY = "60806040523480156200001157600080fd5b50604051620024e0380380620024e0833981810160405260408110156200003757600080fd5b81019080805160405193929190846401000000008211156200005857600080fd5b838201915060208201858111156200006f57600080fd5b82518660018202830111640100000000821117156200008d57600080fd5b8083526020830192505050908051906020019080838360005b83811015620000c3578082015181840152602081019050620000a6565b50505050905090810190601f168015620000f15780820380516001836020036101000a031916815260200191505b50604052602001805160405193929190846401000000008211156200011557600080fd5b838201915060208201858111156200012c57600080fd5b82518660018202830111640100000000821117156200014a57600080fd5b8083526020830192505050908051906020019080838360005b838110156200018057808201518184015260208101905062000163565b50505050905090810190601f168015620001ae5780820380516001836020036101000a031916815260200191505b5060405250505033600660006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555081600790805190602001906200020e929190620004a9565b50806008908051906020019062000227929190620004a9565b5060008060008060001b6000806000801b6000604051602001808860ff1660ff1660f81b81526001018760ff1660ff1660f81b81526001018681526020018560ff1660ff1660f81b81526001018460ff1660ff1660f81b8152600101838152602001828152602001975050505050505050604051602081830303815290604052805190602001209050620002ba62000530565b60405180606001604052806040518060600160405280600060ff168152602001600060ff1681526020016000801b81525081526020016040518060600160405280600060ff168152602001600060ff1681526020016000801b815250815260200160008152509050600981908060018154018082558091505060019003906000526020600020906005020160009091909190915060008201518160000160008201518160000160006101000a81548160ff021916908360ff16021790555060208201518160000160016101000a81548160ff021916908360ff16021790555060408201518160010155505060208201518160020160008201518160000160006101000a81548160ff021916908360ff16021790555060208201518160000160016101000a81548160ff021916908360ff16021790555060408201518160010155505060408201518160040155505060008060008481526020019081526020016000208190555060006001600080815260200190815260200160002060006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555060006002600080815260200190815260200160002060006101000a81548160ff02191690831515021790555050505050620005b3565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f10620004ec57805160ff19168380011785556200051d565b828001600101855582156200051d579182015b828111156200051c578251825591602001919060010190620004ff565b5b5090506200052c919062000561565b5090565b60405180606001604052806200054562000589565b81526020016200055462000589565b8152602001600081525090565b6200058691905b808211156200058257600081600090555060010162000568565b5090565b90565b6040518060600160405280600060ff168152602001600060ff168152602001600080191681525090565b611f1d80620005c36000396000f3fe608060405234801561001057600080fd5b506004361061012c5760003560e01c806381745821116100ad578063b2e6ceeb11610071578063b2e6ceeb14610651578063b88d4fde1461067f578063e985e9c514610784578063f0761fb614610800578063fc0e74d1146108585761012c565b806381745821146104835780638c7a63ae146105115780638da5cb5b146105955780638f32d59b146105df578063a22cb465146106015761012c565b806342842e0e116100f457806342842e0e1461031757806342966c68146103855780635fd8c710146103b35780636352211e146103bd57806370a082311461042b5761012c565b8063081812fc14610131578063095ea7b31461019f57806323b872dd146101ed57806328fcc8901461025b5780632af3c83a146102d1575b600080fd5b61015d6004803603602081101561014757600080fd5b8101908080359060200190929190505050610862565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b6101eb600480360360408110156101b557600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff169060200190929190803590602001909291905050506108b1565b005b6102596004803603606081101561020357600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff169060200190929190803573ffffffffffffffffffffffffffffffffffffffff169060200190929190803590602001909291905050506109cd565b005b6102cf600480360360e081101561027157600080fd5b81019080803560ff169060200190929190803560ff16906020019092919080359060200190929190803560ff169060200190929190803560ff1690602001909291908035906020019092919080359060200190929190505050610c7f565b005b6102fd600480360360208110156102e757600080fd5b81019080803590602001909291905050506110e4565b604051808215151515815260200191505060405180910390f35b6103836004803603606081101561032d57600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff169060200190929190803573ffffffffffffffffffffffffffffffffffffffff1690602001909291908035906020019092919050505061110e565b005b6103b16004803603602081101561039b57600080fd5b810190808035906020019092919050505061112e565b005b6103bb61129e565b005b6103e9600480360360208110156103d357600080fd5b8101908080359060200190929190505050611361565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b61046d6004803603602081101561044157600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff16906020019092919050505061139e565b6040518082815260200191505060405180910390f35b6104f7600480360360e081101561049957600080fd5b81019080803560ff169060200190929190803560ff16906020019092919080359060200190929190803560ff169060200190929190803560ff16906020019092919080359060200190929190803590602001909291905050506113e7565b604051808215151515815260200191505060405180910390f35b61053d6004803603602081101561052757600080fd5b810190808035906020019092919050505061149d565b604051808860ff1660ff1681526020018760ff1660ff1681526020018681526020018560ff1660ff1681526020018460ff1660ff16815260200183815260200182815260200197505050505050505060405180910390f35b61059d6115e0565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b6105e761160a565b604051808215151515815260200191505060405180910390f35b61064f6004803603604081101561061757600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff169060200190929190803515159060200190929190505050611662565b005b61067d6004803603602081101561066757600080fd5b81019080803590602001909291905050506117d6565b005b6107826004803603608081101561069557600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff169060200190929190803573ffffffffffffffffffffffffffffffffffffffff16906020019092919080359060200190929190803590602001906401000000008111156106fc57600080fd5b82018360208201111561070e57600080fd5b8035906020019184600183028401116401000000008311171561073057600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f820116905080830192505050505050509192919290505050611805565b005b6107e66004803603604081101561079a57600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff169060200190929190803573ffffffffffffffffffffffffffffffffffffffff16906020019092919050505061182b565b604051808215151515815260200191505060405180910390f35b6108426004803603602081101561081657600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff1690602001909291905050506118bf565b6040518082815260200191505060405180910390f35b6108606118d7565b005b600061086d826110e4565b61087657600080fd5b6004600083815260200190815260200160002060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff169050919050565b6001600082815260200190815260200160002060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff161461091c57600080fd5b816004600083815260200190815260200160002060006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550808273ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff167f8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b92560405160405180910390a45050565b600073ffffffffffffffffffffffffffffffffffffffff168373ffffffffffffffffffffffffffffffffffffffff1614158015610a375750600073ffffffffffffffffffffffffffffffffffffffff168273ffffffffffffffffffffffffffffffffffffffff1614155b610a4057600080fd5b610a4a338261196a565b610a5357600080fd5b610a5c816110e4565b610a6557600080fd5b816001600083815260200190815260200160002060006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550610ac18282611a2a565b610b0b600360008473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020546001611b66565b600360008473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002081905550610b98600360008573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020546001611bee565b600360008573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020819055507f9c8515990fd8c61431c4ac8db9b81475f90c292a1dda77731e56c22e64fc7643838383604051808473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020018373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001828152602001935050505060405180910390a1505050565b610c8e878787878787876113e7565b15610d01576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004018080602001828103825260198152602001807f5468697320746f6b656e20616c7265616479206578697374210000000000000081525060200191505060405180910390fd5b60008114158015610d185750610d16816110e4565b155b15610d6e576040517f08c379a0000000000000000000000000000000000000000000000000000000008152600401808060200182810382526033815260200180611eb56033913960400191505060405180910390fd5b600087878787878787604051602001808860ff1660ff1660f81b81526001018760ff1660ff1660f81b81526001018681526020018560ff1660ff1660f81b81526001018460ff1660ff1660f81b8152600101838152602001828152602001975050505050505050604051602081830303815290604052805190602001209050610df5611e5d565b604051806060016040528060405180606001604052808c60ff1681526020018b60ff1681526020018a815250815260200160405180606001604052808960ff1681526020018860ff168152602001878152508152602001848152509050600981908060018154018082558091505060019003906000526020600020906005020160009091909190915060008201518160000160008201518160000160006101000a81548160ff021916908360ff16021790555060208201518160000160016101000a81548160ff021916908360ff16021790555060408201518160010155505060208201518160020160008201518160000160006101000a81548160ff021916908360ff16021790555060208201518160000160016101000a81548160ff021916908360ff160217905550604082015181600101555050604082015181600401555050600060098054905090508080600190039150508060008085815260200190815260200160002081905550336001600083815260200190815260200160002060006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555060016002600083815260200190815260200160002060006101000a81548160ff02191690831515021790555061102a600360003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020546001611b66565b600360003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020819055507f55d785d3e2a7effd275e725eb0fd1a1457366d1d3709f9bb61cd2d83913fdb838133604051808381526020018273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019250505060405180910390a150505050505050505050565b60006002600083815260200190815260200160002060009054906101000a900460ff169050919050565b61112983838360405180602001604052806000815250611805565b505050565b6001600082815260200190815260200160002060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff161461119957600080fd5b6111a2816110e4565b6111ab57600080fd5b60006002600083815260200190815260200160002060006101000a81548160ff021916908315150217905550611221600360003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020546001611bee565b600360003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020819055507f693be48935476ffdb3775faab46f5c46604e07fccc528be67332d2fb73a6b2db816040518082815260200191505060405180910390a150565b6112a661160a565b611318576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004018080602001828103825260208152602001807f4f776e61626c653a2063616c6c6572206973206e6f7420746865206f776e657281525060200191505060405180910390fd5b3373ffffffffffffffffffffffffffffffffffffffff166108fc479081150290604051600060405180830381858888f1935050505015801561135e573d6000803e3d6000fd5b50565b60006001600083815260200190815260200160002060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff169050919050565b6000600360008373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020549050919050565b60008088888888888888604051602001808860ff1660ff1660f81b81526001018760ff1660ff1660f81b81526001018681526020018560ff1660ff1660f81b81526001018460ff1660ff1660f81b8152600101838152602001828152602001975050505050505050604051602081830303815290604052805190602001209050600080600083815260200190815260200160002054141561148c576000915050611492565b60019150505b979650505050505050565b6000806000806000806000600988815481106114b557fe5b906000526020600020906005020160000160000160009054906101000a900460ff16600989815481106114e457fe5b906000526020600020906005020160000160000160019054906101000a900460ff1660098a8154811061151357fe5b90600052602060002090600502016000016001015460098b8154811061153557fe5b906000526020600020906005020160020160000160009054906101000a900460ff1660098c8154811061156457fe5b906000526020600020906005020160020160000160019054906101000a900460ff1660098d8154811061159357fe5b90600052602060002090600502016002016001015460098e815481106115b557fe5b9060005260206000209060050201600401549650965096509650965096509650919395979092949650565b6000600660009054906101000a900473ffffffffffffffffffffffffffffffffffffffff16905090565b6000600660009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff1614905090565b3373ffffffffffffffffffffffffffffffffffffffff168273ffffffffffffffffffffffffffffffffffffffff16141561169b57600080fd5b80600560003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060008473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060006101000a81548160ff0219169083151502179055507f17307eab39ab6107e8899845ad3d59bd9653f200f220920489ca2b5937696c31338383604051808473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020018373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200182151515158152602001935050505060405180910390a15050565b6117e0338261196a565b6117e957600080fd5b60006117f482611361565b90506118018133846109cd565b5050565b6118108484846109cd565b61181c84848484611c77565b61182557600080fd5b50505050565b6000600560008473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060008373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060009054906101000a900460ff16905092915050565b60036020528060005260406000206000915090505481565b6118df61160a565b611951576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004018080602001828103825260208152602001807f4f776e61626c653a2063616c6c6572206973206e6f7420746865206f776e657281525060200191505060405180910390fd5b3373ffffffffffffffffffffffffffffffffffffffff16ff5b6000806001600084815260200190815260200160002060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1690508073ffffffffffffffffffffffffffffffffffffffff168473ffffffffffffffffffffffffffffffffffffffff161480611a1057508373ffffffffffffffffffffffffffffffffffffffff166119f884610862565b73ffffffffffffffffffffffffffffffffffffffff16145b80611a215750611a20818561182b565b5b91505092915050565b8173ffffffffffffffffffffffffffffffffffffffff166001600083815260200190815260200160002060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1614611a9557600080fd5b611a9e816110e4565b611aa757600080fd5b600073ffffffffffffffffffffffffffffffffffffffff166004600083815260200190815260200160002060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1614611b625760006004600083815260200190815260200160002060006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055505b5050565b600080828401905083811015611be4576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040180806020018281038252601b8152602001807f536166654d6174683a206164646974696f6e206f766572666c6f77000000000081525060200191505060405180910390fd5b8091505092915050565b600082821115611c66576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040180806020018281038252601e8152602001807f536166654d6174683a207375627472616374696f6e206f766572666c6f77000081525060200191505060405180910390fd5b600082840390508091505092915050565b6000611c8284611e4a565b611c8f5760019050611e42565b60008473ffffffffffffffffffffffffffffffffffffffff1663150b7a02338887876040518563ffffffff1660e01b8152600401808573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020018473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200183815260200180602001828103825283818151815260200191508051906020019080838360005b83811015611d6a578082015181840152602081019050611d4f565b50505050905090810190601f168015611d975780820380516001836020036101000a031916815260200191505b5095505050505050602060405180830381600087803b158015611db957600080fd5b505af1158015611dcd573d6000803e3d6000fd5b505050506040513d6020811015611de357600080fd5b8101908080519060200190929190505050905063150b7a0260e01b7bffffffffffffffffffffffffffffffffffffffffffffffffffffffff1916817bffffffffffffffffffffffffffffffffffffffffffffffffffffffff1916149150505b949350505050565b600080823b905060008111915050919050565b6040518060600160405280611e70611e8a565b8152602001611e7d611e8a565b8152602001600081525090565b6040518060600160405280600060ff168152602001600060ff16815260200160008019168152509056fe50726576696f757320746f6b656e20646f6573206e6f7420657869737473206f7220646966666572656e742066726f6d203021a264697066735822122075304ee50abc10bc036b4a7022020bc823e4c51a82c1fcce96ffbb9979bbce4a64736f6c63430006040033";
    public static final String FUNC_APPROVE = "approve";

    public static final String FUNC_BALANCEOF = "balanceOf";

    public static final String FUNC_BURN = "burn";

    public static final String FUNC_GETAPPROVED = "getApproved";

    public static final String FUNC_GETTOKENINFO = "getTokenInfo";

    public static final String FUNC_ISAPPROVEDFORALL = "isApprovedForAll";

    public static final String FUNC_ISOWNER = "isOwner";

    public static final String FUNC_MINT = "mint";

    public static final String FUNC_OWNER = "owner";

    public static final String FUNC_OWNEROF = "ownerOf";

    public static final String FUNC_OWNERONTOCOUNT = "ownerOntoCount";

    public static final String FUNC_safeTransferFrom = "safeTransferFrom";

    public static final String FUNC_SETAPPROVALFORALL = "setApprovalForAll";

    public static final String FUNC_SHUTDOWN = "shutdown";

    public static final String FUNC_TAKEOWNERSHIP = "takeOwnership";

    public static final String FUNC_TOKENEXISTS = "tokenExists";

    public static final String FUNC_TOKENIDEXISTS = "tokenIDExists";

    public static final String FUNC_TRANSFERFROM = "transferFrom";

    public static final String FUNC_WITHDRAWBALANCE = "withdrawBalance";

    public static final Event APPROVAL_EVENT = new Event("Approval", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Uint256>(true) {}));
    ;

    public static final Event APPROVALFORALL_EVENT = new Event("ApprovalForAll", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Address>() {}, new TypeReference<Bool>() {}));
    ;

    public static final Event TOKENTRANSFERRED_EVENT = new Event("TokenTransferred", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Address>() {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event TRANSFER_EVENT = new Event("Transfer", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Uint256>(true) {}));
    ;

    public static final Event TOKENBURNED_EVENT = new Event("tokenBurned", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
    ;

    public static final Event TOKENMINTED_EVENT = new Event("tokenMinted", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Address>() {}));
    ;

    @Deprecated
    protected Oasisosc(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected Oasisosc(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected Oasisosc(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected Oasisosc(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public List<ApprovalEventResponse> getApprovalEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(APPROVAL_EVENT, transactionReceipt);
        ArrayList<ApprovalEventResponse> responses = new ArrayList<ApprovalEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            ApprovalEventResponse typedResponse = new ApprovalEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.owner = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.approved = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.tokenId = (BigInteger) eventValues.getIndexedValues().get(2).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<ApprovalEventResponse> approvalEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, ApprovalEventResponse>() {
            @Override
            public ApprovalEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(APPROVAL_EVENT, log);
                ApprovalEventResponse typedResponse = new ApprovalEventResponse();
                typedResponse.log = log;
                typedResponse.owner = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.approved = (String) eventValues.getIndexedValues().get(1).getValue();
                typedResponse.tokenId = (BigInteger) eventValues.getIndexedValues().get(2).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<ApprovalEventResponse> approvalEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(APPROVAL_EVENT));
        return approvalEventFlowable(filter);
    }

    public List<ApprovalForAllEventResponse> getApprovalForAllEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(APPROVALFORALL_EVENT, transactionReceipt);
        ArrayList<ApprovalForAllEventResponse> responses = new ArrayList<ApprovalForAllEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            ApprovalForAllEventResponse typedResponse = new ApprovalForAllEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.from = (String) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.to = (String) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.approved = (Boolean) eventValues.getNonIndexedValues().get(2).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<ApprovalForAllEventResponse> approvalForAllEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, ApprovalForAllEventResponse>() {
            @Override
            public ApprovalForAllEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(APPROVALFORALL_EVENT, log);
                ApprovalForAllEventResponse typedResponse = new ApprovalForAllEventResponse();
                typedResponse.log = log;
                typedResponse.from = (String) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.to = (String) eventValues.getNonIndexedValues().get(1).getValue();
                typedResponse.approved = (Boolean) eventValues.getNonIndexedValues().get(2).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<ApprovalForAllEventResponse> approvalForAllEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(APPROVALFORALL_EVENT));
        return approvalForAllEventFlowable(filter);
    }

    public List<TokenTransferredEventResponse> getTokenTransferredEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(TOKENTRANSFERRED_EVENT, transactionReceipt);
        ArrayList<TokenTransferredEventResponse> responses = new ArrayList<TokenTransferredEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            TokenTransferredEventResponse typedResponse = new TokenTransferredEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.from = (String) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.to = (String) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.tokenId = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<TokenTransferredEventResponse> tokenTransferredEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, TokenTransferredEventResponse>() {
            @Override
            public TokenTransferredEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(TOKENTRANSFERRED_EVENT, log);
                TokenTransferredEventResponse typedResponse = new TokenTransferredEventResponse();
                typedResponse.log = log;
                typedResponse.from = (String) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.to = (String) eventValues.getNonIndexedValues().get(1).getValue();
                typedResponse.tokenId = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<TokenTransferredEventResponse> tokenTransferredEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(TOKENTRANSFERRED_EVENT));
        return tokenTransferredEventFlowable(filter);
    }

    public List<TransferEventResponse> getTransferEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(TRANSFER_EVENT, transactionReceipt);
        ArrayList<TransferEventResponse> responses = new ArrayList<TransferEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            TransferEventResponse typedResponse = new TransferEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.from = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.to = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.tokenId = (BigInteger) eventValues.getIndexedValues().get(2).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<TransferEventResponse> transferEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, TransferEventResponse>() {
            @Override
            public TransferEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(TRANSFER_EVENT, log);
                TransferEventResponse typedResponse = new TransferEventResponse();
                typedResponse.log = log;
                typedResponse.from = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.to = (String) eventValues.getIndexedValues().get(1).getValue();
                typedResponse.tokenId = (BigInteger) eventValues.getIndexedValues().get(2).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<TransferEventResponse> transferEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(TRANSFER_EVENT));
        return transferEventFlowable(filter);
    }

    public List<TokenBurnedEventResponse> getTokenBurnedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(TOKENBURNED_EVENT, transactionReceipt);
        ArrayList<TokenBurnedEventResponse> responses = new ArrayList<TokenBurnedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            TokenBurnedEventResponse typedResponse = new TokenBurnedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.tokenId = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<TokenBurnedEventResponse> tokenBurnedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, TokenBurnedEventResponse>() {
            @Override
            public TokenBurnedEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(TOKENBURNED_EVENT, log);
                TokenBurnedEventResponse typedResponse = new TokenBurnedEventResponse();
                typedResponse.log = log;
                typedResponse.tokenId = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<TokenBurnedEventResponse> tokenBurnedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(TOKENBURNED_EVENT));
        return tokenBurnedEventFlowable(filter);
    }

    public List<TokenMintedEventResponse> getTokenMintedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(TOKENMINTED_EVENT, transactionReceipt);
        ArrayList<TokenMintedEventResponse> responses = new ArrayList<TokenMintedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            TokenMintedEventResponse typedResponse = new TokenMintedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.tokenId = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.owner = (String) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<TokenMintedEventResponse> tokenMintedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, TokenMintedEventResponse>() {
            @Override
            public TokenMintedEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(TOKENMINTED_EVENT, log);
                TokenMintedEventResponse typedResponse = new TokenMintedEventResponse();
                typedResponse.log = log;
                typedResponse.tokenId = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.owner = (String) eventValues.getNonIndexedValues().get(1).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<TokenMintedEventResponse> tokenMintedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(TOKENMINTED_EVENT));
        return tokenMintedEventFlowable(filter);
    }

    public RemoteFunctionCall<TransactionReceipt> approve(String _to, BigInteger id) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_APPROVE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _to), 
                new org.web3j.abi.datatypes.generated.Uint256(id)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<BigInteger> balanceOf(String _tokenOwner) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_BALANCEOF, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _tokenOwner)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> burn(BigInteger id) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_BURN, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(id)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<String> getApproved(BigInteger id) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_GETAPPROVED, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(id)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<Tuple7<BigInteger, BigInteger, byte[], BigInteger, BigInteger, byte[], BigInteger>> getTokenInfo(BigInteger id) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_GETTOKENINFO, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(id)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint8>() {}, new TypeReference<Uint8>() {}, new TypeReference<Bytes32>() {}, new TypeReference<Uint8>() {}, new TypeReference<Uint8>() {}, new TypeReference<Bytes32>() {}, new TypeReference<Uint256>() {}));
        return new RemoteFunctionCall<Tuple7<BigInteger, BigInteger, byte[], BigInteger, BigInteger, byte[], BigInteger>>(function,
                new Callable<Tuple7<BigInteger, BigInteger, byte[], BigInteger, BigInteger, byte[], BigInteger>>() {
                    @Override
                    public Tuple7<BigInteger, BigInteger, byte[], BigInteger, BigInteger, byte[], BigInteger> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple7<BigInteger, BigInteger, byte[], BigInteger, BigInteger, byte[], BigInteger>(
                                (BigInteger) results.get(0).getValue(), 
                                (BigInteger) results.get(1).getValue(), 
                                (byte[]) results.get(2).getValue(), 
                                (BigInteger) results.get(3).getValue(), 
                                (BigInteger) results.get(4).getValue(), 
                                (byte[]) results.get(5).getValue(), 
                                (BigInteger) results.get(6).getValue());
                    }
                });
    }

    public RemoteFunctionCall<Boolean> isApprovedForAll(String Owner, String operator) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_ISAPPROVEDFORALL, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, Owner), 
                new org.web3j.abi.datatypes.Address(160, operator)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteFunctionCall<Boolean> isOwner() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_ISOWNER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteFunctionCall<TransactionReceipt> mint(BigInteger hashO, BigInteger sizeO, byte[] digestO, BigInteger hashQ, BigInteger sizeQ, byte[] digestQ, BigInteger prev) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_MINT, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint8(hashO), 
                new org.web3j.abi.datatypes.generated.Uint8(sizeO), 
                new org.web3j.abi.datatypes.generated.Bytes32(digestO), 
                new org.web3j.abi.datatypes.generated.Uint8(hashQ), 
                new org.web3j.abi.datatypes.generated.Uint8(sizeQ), 
                new org.web3j.abi.datatypes.generated.Bytes32(digestQ), 
                new org.web3j.abi.datatypes.generated.Uint256(prev)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<String> owner() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_OWNER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<String> ownerOf(BigInteger id) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_OWNEROF, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(id)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<BigInteger> ownerOntoCount(String param0) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_OWNERONTOCOUNT, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> safeTransferFrom(String from, String to, BigInteger id) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_safeTransferFrom, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, from), 
                new org.web3j.abi.datatypes.Address(160, to), 
                new org.web3j.abi.datatypes.generated.Uint256(id)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> safeTransferFrom(String from, String to, BigInteger id, byte[] _data) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_safeTransferFrom, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, from), 
                new org.web3j.abi.datatypes.Address(160, to), 
                new org.web3j.abi.datatypes.generated.Uint256(id), 
                new org.web3j.abi.datatypes.DynamicBytes(_data)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> setApprovalForAll(String to, Boolean approved) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_SETAPPROVALFORALL, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, to), 
                new org.web3j.abi.datatypes.Bool(approved)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> shutdown() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_SHUTDOWN, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> takeOwnership(BigInteger id) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_TAKEOWNERSHIP, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(id)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<Boolean> tokenExists(BigInteger hashO, BigInteger sizeO, byte[] digestO, BigInteger hashQ, BigInteger sizeQ, byte[] digestQ, BigInteger prev) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_TOKENEXISTS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint8(hashO), 
                new org.web3j.abi.datatypes.generated.Uint8(sizeO), 
                new org.web3j.abi.datatypes.generated.Bytes32(digestO), 
                new org.web3j.abi.datatypes.generated.Uint8(hashQ), 
                new org.web3j.abi.datatypes.generated.Uint8(sizeQ), 
                new org.web3j.abi.datatypes.generated.Bytes32(digestQ), 
                new org.web3j.abi.datatypes.generated.Uint256(prev)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteFunctionCall<Boolean> tokenIDExists(BigInteger id) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_TOKENIDEXISTS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(id)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteFunctionCall<TransactionReceipt> transferFrom(String from, String to, BigInteger id) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_TRANSFERFROM, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, from), 
                new org.web3j.abi.datatypes.Address(160, to), 
                new org.web3j.abi.datatypes.generated.Uint256(id)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> withdrawBalance() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_WITHDRAWBALANCE, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @Deprecated
    public static Oasisosc load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new Oasisosc(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static Oasisosc load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new Oasisosc(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static Oasisosc load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new Oasisosc(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static Oasisosc load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new Oasisosc(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<Oasisosc> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider, String name, String symbol) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(name), 
                new org.web3j.abi.datatypes.Utf8String(symbol)));
        return deployRemoteCall(Oasisosc.class, web3j, credentials, contractGasProvider, BINARY, encodedConstructor);
    }

    public static RemoteCall<Oasisosc> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider, String name, String symbol) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(name), 
                new org.web3j.abi.datatypes.Utf8String(symbol)));
        return deployRemoteCall(Oasisosc.class, web3j, transactionManager, contractGasProvider, BINARY, encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<Oasisosc> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit, String name, String symbol) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(name), 
                new org.web3j.abi.datatypes.Utf8String(symbol)));
        return deployRemoteCall(Oasisosc.class, web3j, credentials, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<Oasisosc> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit, String name, String symbol) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(name), 
                new org.web3j.abi.datatypes.Utf8String(symbol)));
        return deployRemoteCall(Oasisosc.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    public static class ApprovalEventResponse extends BaseEventResponse {
        public String owner;

        public String approved;

        public BigInteger tokenId;
    }

    public static class ApprovalForAllEventResponse extends BaseEventResponse {
        public String from;

        public String to;

        public Boolean approved;
    }

    public static class TokenTransferredEventResponse extends BaseEventResponse {
        public String from;

        public String to;

        public BigInteger tokenId;
    }

    public static class TransferEventResponse extends BaseEventResponse {
        public String from;

        public String to;

        public BigInteger tokenId;
    }

    public static class TokenBurnedEventResponse extends BaseEventResponse {
        public BigInteger tokenId;
    }

    public static class TokenMintedEventResponse extends BaseEventResponse {
        public BigInteger tokenId;

        public String owner;
    }
}

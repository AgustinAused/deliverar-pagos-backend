package com.deliverar.pagos.domain.entities;

import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
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
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/LFDT-web3j/web3j/tree/main/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 1.7.0.
 */
@SuppressWarnings("rawtypes")
public class DeliverCoin extends Contract {
    public static final String BINARY = "60c0604052600b60809081526a2232b634bb32b921b7b4b760a91b60a0525f9061002990826101b1565b50604080518082019091526002815261444360f01b602082015260019061005090826101b1565b506002805460ff191681179055348015610068575f5ffd5b50604051610cdf380380610cdf8339810160408190526100879161026b565b600480546001600160a01b0319163317905560038290556040515f906100b1908390602001610326565b60408051601f1981840301815282825280516020918201205f818152600590925291902085905591507f5358be4df107be4d9b023fc323f41d7109610225c6ef211b9d375b9fbd7ccc4f90610109908490869061033c565b60405180910390a1505050610385565b634e487b7160e01b5f52604160045260245ffd5b600181811c9082168061014157607f821691505b60208210810361015f57634e487b7160e01b5f52602260045260245ffd5b50919050565b601f8211156101ac57805f5260205f20601f840160051c8101602085101561018a5750805b601f840160051c820191505b818110156101a9575f8155600101610196565b50505b505050565b81516001600160401b038111156101ca576101ca610119565b6101de816101d8845461012d565b84610165565b6020601f821160018114610210575f83156101f95750848201515b5f19600385901b1c1916600184901b1784556101a9565b5f84815260208120601f198516915b8281101561023f578785015182556020948501946001909201910161021f565b508482101561025c57868401515f19600387901b60f8161c191681555b50505050600190811b01905550565b5f5f6040838503121561027c575f5ffd5b825160208401519092506001600160401b03811115610299575f5ffd5b8301601f810185136102a9575f5ffd5b80516001600160401b038111156102c2576102c2610119565b604051601f8201601f19908116603f011681016001600160401b03811182821017156102f0576102f0610119565b604052818152828201602001871015610307575f5ffd5b8160208401602083015e5f602083830101528093505050509250929050565b5f82518060208501845e5f920191825250919050565b606081525f6060820152608060208201525f8351806080840152806020860160a085015e5f60a0828501015260a0601f19601f8301168401019150508260408301529392505050565b61094d806103925f395ff3fe608060405234801561000f575f5ffd5b5060043610610090575f3560e01c80637641e6f3116100635780637641e6f3146100f657806377097fc81461010b5780638da5cb5b1461011e57806395d89b41146101495780639b80b05014610151575f5ffd5b806306fdde031461009457806318160ddd146100b2578063313ce567146100c457806335ee5f87146100e3575b5f5ffd5b61009c610174565b6040516100a9919061061e565b60405180910390f35b6003545b6040519081526020016100a9565b6002546100d19060ff1681565b60405160ff90911681526020016100a9565b6100b66100f13660046106cf565b6101ff565b610109610104366004610709565b61023d565b005b610109610119366004610709565b610370565b600454610131906001600160a01b031681565b6040516001600160a01b0390911681526020016100a9565b61009c610437565b61016461015f36600461074d565b610444565b60405190151581526020016100a9565b5f8054610180906107bb565b80601f01602080910402602001604051908101604052809291908181526020018280546101ac906107bb565b80156101f75780601f106101ce576101008083540402835291602001916101f7565b820191905f5260205f20905b8154815290600101906020018083116101da57829003601f168201915b505050505081565b5f5f8260405160200161021291906107f3565b60408051601f1981840301815291815281516020928301205f90815260059092529020549392505050565b6004546001600160a01b031633146102705760405162461bcd60e51b815260040161026790610809565b60405180910390fd5b5f8160405160200161028291906107f3565b6040516020818303038152906040528051906020012090508260055f8381526020019081526020015f205410156102fb5760405162461bcd60e51b815260206004820152601c60248201527f496e73756666696369656e742062616c616e636520746f206275726e000000006044820152606401610267565b5f8181526005602052604090205461031390846105ae565b5f8281526005602052604090205560035461032e90846105ae565b6003556040517f5358be4df107be4d9b023fc323f41d7109610225c6ef211b9d375b9fbd7ccc4f906103639084908690610835565b60405180910390a1505050565b6004546001600160a01b0316331461039a5760405162461bcd60e51b815260040161026790610809565b5f816040516020016103ac91906107f3565b6040516020818303038152906040528051906020012090506103d9836003546105d290919063ffffffff16565b6003555f818152600560205260409020546103f490846105d2565b5f828152600560205260409081902091909155517f5358be4df107be4d9b023fc323f41d7109610225c6ef211b9d375b9fbd7ccc4f906103639084908690610866565b60018054610180906107bb565b6004545f906001600160a01b031633146104705760405162461bcd60e51b815260040161026790610809565b5f8460405160200161048291906107f3565b6040516020818303038152906040528051906020012090505f846040516020016104ac91906107f3565b6040516020818303038152906040528051906020012090508360055f8481526020019081526020015f2054101561051c5760405162461bcd60e51b8152602060048201526014602482015273496e73756666696369656e742062616c616e636560601b6044820152606401610267565b5f8281526005602052604090205461053490856105ae565b5f8381526005602052604080822092909255828152205461055590856105d2565b5f828152600560205260409081902091909155517f5358be4df107be4d9b023fc323f41d7109610225c6ef211b9d375b9fbd7ccc4f9061059a90889088908890610894565b60405180910390a150600195945050505050565b5f828211156105bf576105bf6108c9565b6105c982846108f1565b90505b92915050565b5f806105de8385610904565b9050838110156105c9576105c96108c9565b5f81518084528060208401602086015e5f602082860101526020601f19601f83011685010191505092915050565b602081525f6105c960208301846105f0565b634e487b7160e01b5f52604160045260245ffd5b5f82601f830112610653575f5ffd5b813567ffffffffffffffff81111561066d5761066d610630565b604051601f8201601f19908116603f0116810167ffffffffffffffff8111828210171561069c5761069c610630565b6040528181528382016020018510156106b3575f5ffd5b816020850160208301375f918101602001919091529392505050565b5f602082840312156106df575f5ffd5b813567ffffffffffffffff8111156106f5575f5ffd5b61070184828501610644565b949350505050565b5f5f6040838503121561071a575f5ffd5b82359150602083013567ffffffffffffffff811115610737575f5ffd5b61074385828601610644565b9150509250929050565b5f5f5f6060848603121561075f575f5ffd5b833567ffffffffffffffff811115610775575f5ffd5b61078186828701610644565b935050602084013567ffffffffffffffff81111561079d575f5ffd5b6107a986828701610644565b93969395505050506040919091013590565b600181811c908216806107cf57607f821691505b6020821081036107ed57634e487b7160e01b5f52602260045260245ffd5b50919050565b5f82518060208501845e5f920191825250919050565b6020808252601290820152712737ba1031b7b73a3930b1ba1037bbb732b960711b604082015260600190565b606081525f61084760608301856105f0565b82810360208401525f8152602081019150508260408301529392505050565b606081525f6060820152608060208201525f61088560808301856105f0565b90508260408301529392505050565b606081525f6108a660608301866105f0565b82810360208401526108b881866105f0565b915050826040830152949350505050565b634e487b7160e01b5f52600160045260245ffd5b634e487b7160e01b5f52601160045260245ffd5b818103818111156105cc576105cc6108dd565b808201808211156105cc576105cc6108dd56fea2646970667358221220bb353e5e5e45f87161319f308e41e59204d3a92d63bcc72763e48d67365d08f664736f6c634300081e0033";

    private static String librariesLinkedBinary;

    public static final String FUNC_BALANCEOF = "balanceOf";

    public static final String FUNC_BURN = "burn";

    public static final String FUNC_DECIMALS = "decimals";

    public static final String FUNC_MINT = "mint";

    public static final String FUNC_NAME = "name";

    public static final String FUNC_OWNER = "owner";

    public static final String FUNC_SYMBOL = "symbol";

    public static final String FUNC_TOTALSUPPLY = "totalSupply";

    public static final String FUNC_TRANSFER = "transfer";

    public static final Event TRANSFER_EVENT = new Event("Transfer", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}));
    ;

    @Deprecated
    protected DeliverCoin(String contractAddress, Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected DeliverCoin(String contractAddress, Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected DeliverCoin(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected DeliverCoin(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static List<TransferEventResponse> getTransferEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(TRANSFER_EVENT, transactionReceipt);
        ArrayList<TransferEventResponse> responses = new ArrayList<TransferEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            TransferEventResponse typedResponse = new TransferEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.fromEmail = (String) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.toEmail = (String) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.value = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static TransferEventResponse getTransferEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(TRANSFER_EVENT, log);
        TransferEventResponse typedResponse = new TransferEventResponse();
        typedResponse.log = log;
        typedResponse.fromEmail = (String) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.toEmail = (String) eventValues.getNonIndexedValues().get(1).getValue();
        typedResponse.value = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
        return typedResponse;
    }

    public Flowable<TransferEventResponse> transferEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getTransferEventFromLog(log));
    }

    public Flowable<TransferEventResponse> transferEventFlowable(DefaultBlockParameter startBlock,
            DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(TRANSFER_EVENT));
        return transferEventFlowable(filter);
    }

    public RemoteFunctionCall<BigInteger> balanceOf(String email) {
        final Function function = new Function(FUNC_BALANCEOF, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(email)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> burn(BigInteger amount, String fromEmail) {
        final Function function = new Function(
                FUNC_BURN, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(amount), 
                new org.web3j.abi.datatypes.Utf8String(fromEmail)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<BigInteger> decimals() {
        final Function function = new Function(FUNC_DECIMALS, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint8>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> mint(BigInteger amount, String toEmail) {
        final Function function = new Function(
                FUNC_MINT, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(amount), 
                new org.web3j.abi.datatypes.Utf8String(toEmail)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<String> name() {
        final Function function = new Function(FUNC_NAME, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<String> owner() {
        final Function function = new Function(FUNC_OWNER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<String> symbol() {
        final Function function = new Function(FUNC_SYMBOL, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<BigInteger> totalSupply() {
        final Function function = new Function(FUNC_TOTALSUPPLY, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> transfer(String fromEmail, String toEmail,
            BigInteger value) {
        final Function function = new Function(
                FUNC_TRANSFER, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(fromEmail), 
                new org.web3j.abi.datatypes.Utf8String(toEmail), 
                new org.web3j.abi.datatypes.generated.Uint256(value)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @Deprecated
    public static DeliverCoin load(String contractAddress, Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        return new DeliverCoin(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static DeliverCoin load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new DeliverCoin(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static DeliverCoin load(String contractAddress, Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        return new DeliverCoin(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static DeliverCoin load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new DeliverCoin(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<DeliverCoin> deploy(Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider, BigInteger initialSupply, String ownerEmail) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(initialSupply), 
                new org.web3j.abi.datatypes.Utf8String(ownerEmail)));
        return deployRemoteCall(DeliverCoin.class, web3j, credentials, contractGasProvider, getDeploymentBinary(), encodedConstructor);
    }

    public static RemoteCall<DeliverCoin> deploy(Web3j web3j, TransactionManager transactionManager,
            ContractGasProvider contractGasProvider, BigInteger initialSupply, String ownerEmail) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(initialSupply), 
                new org.web3j.abi.datatypes.Utf8String(ownerEmail)));
        return deployRemoteCall(DeliverCoin.class, web3j, transactionManager, contractGasProvider, getDeploymentBinary(), encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<DeliverCoin> deploy(Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit, BigInteger initialSupply, String ownerEmail) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(initialSupply), 
                new org.web3j.abi.datatypes.Utf8String(ownerEmail)));
        return deployRemoteCall(DeliverCoin.class, web3j, credentials, gasPrice, gasLimit, getDeploymentBinary(), encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<DeliverCoin> deploy(Web3j web3j, TransactionManager transactionManager,
            BigInteger gasPrice, BigInteger gasLimit, BigInteger initialSupply, String ownerEmail) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(initialSupply), 
                new org.web3j.abi.datatypes.Utf8String(ownerEmail)));
        return deployRemoteCall(DeliverCoin.class, web3j, transactionManager, gasPrice, gasLimit, getDeploymentBinary(), encodedConstructor);
    }

    public static void linkLibraries(List<Contract.LinkReference> references) {
        librariesLinkedBinary = linkBinaryWithReferences(BINARY, references);
    }

    private static String getDeploymentBinary() {
        if (librariesLinkedBinary != null) {
            return librariesLinkedBinary;
        } else {
            return BINARY;
        }
    }

    public static class TransferEventResponse extends BaseEventResponse {
        public String fromEmail;

        public String toEmail;

        public BigInteger value;
    }
}

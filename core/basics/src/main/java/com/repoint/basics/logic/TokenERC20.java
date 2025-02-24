package com.repoint.basics.logic;

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
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TokenERC20 extends Contract {
    private static final String BINARY = "608060405234801562000010575f80fd5b5060405162001670380380620016708339818101604052810190620000369190620003cb565b6040518060400160405280600781526020017f4d79546f6b656e000000000000000000000000000000000000000000000000008152506040518060400160405280600381526020017f4d544b00000000000000000000000000000000000000000000000000000000008152508160039081620000b3919062000656565b508060049081620000c5919062000656565b505050620000da3382620000e160201b60201c565b5062000866565b5f73ffffffffffffffffffffffffffffffffffffffff168273ffffffffffffffffffffffffffffffffffffffff160362000154575f6040517fec442f050000000000000000000000000000000000000000000000000000000081526004016200014b91906200077d565b60405180910390fd5b620001675f83836200016b60201b60201c565b5050565b5f73ffffffffffffffffffffffffffffffffffffffff168373ffffffffffffffffffffffffffffffffffffffff1603620001bf578060025f828254620001b29190620007c5565b9250508190555062000290565b5f805f8573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020015f20549050818110156200024b578381836040517fe450d38c000000000000000000000000000000000000000000000000000000008152600401620002429392919062000810565b60405180910390fd5b8181035f808673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020015f2081905550505b5f73ffffffffffffffffffffffffffffffffffffffff168273ffffffffffffffffffffffffffffffffffffffff1603620002d9578060025f828254039250508190555062000323565b805f808473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020015f205f82825401925050819055505b8173ffffffffffffffffffffffffffffffffffffffff168373ffffffffffffffffffffffffffffffffffffffff167fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef836040516200038291906200084b565b60405180910390a3505050565b5f80fd5b5f819050919050565b620003a78162000393565b8114620003b2575f80fd5b50565b5f81519050620003c5816200039c565b92915050565b5f60208284031215620003e357620003e26200038f565b5b5f620003f284828501620003b5565b91505092915050565b5f81519050919050565b7f4e487b71000000000000000000000000000000000000000000000000000000005f52604160045260245ffd5b7f4e487b71000000000000000000000000000000000000000000000000000000005f52602260045260245ffd5b5f60028204905060018216806200047757607f821691505b6020821081036200048d576200048c62000432565b5b50919050565b5f819050815f5260205f209050919050565b5f6020601f8301049050919050565b5f82821b905092915050565b5f60088302620004f17fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff82620004b4565b620004fd8683620004b4565b95508019841693508086168417925050509392505050565b5f819050919050565b5f6200053e62000538620005328462000393565b62000515565b62000393565b9050919050565b5f819050919050565b62000559836200051e565b62000571620005688262000545565b848454620004c0565b825550505050565b5f90565b6200058762000579565b620005948184846200054e565b505050565b5b81811015620005bb57620005af5f826200057d565b6001810190506200059a565b5050565b601f8211156200060a57620005d48162000493565b620005df84620004a5565b81016020851015620005ef578190505b62000607620005fe85620004a5565b83018262000599565b50505b505050565b5f82821c905092915050565b5f6200062c5f19846008026200060f565b1980831691505092915050565b5f6200064683836200061b565b9150826002028217905092915050565b6200066182620003fb565b67ffffffffffffffff8111156200067d576200067c62000405565b5b6200068982546200045f565b62000696828285620005bf565b5f60209050601f831160018114620006cc575f8415620006b7578287015190505b620006c3858262000639565b86555062000732565b601f198416620006dc8662000493565b5f5b828110156200070557848901518255600182019150602085019450602081019050620006de565b8683101562000725578489015162000721601f8916826200061b565b8355505b6001600288020188555050505b505050505050565b5f73ffffffffffffffffffffffffffffffffffffffff82169050919050565b5f62000765826200073a565b9050919050565b620007778162000759565b82525050565b5f602082019050620007925f8301846200076c565b92915050565b7f4e487b71000000000000000000000000000000000000000000000000000000005f52601160045260245ffd5b5f620007d18262000393565b9150620007de8362000393565b9250828201905080821115620007f957620007f862000798565b5b92915050565b6200080a8162000393565b82525050565b5f606082019050620008255f8301866200076c565b620008346020830185620007ff565b620008436040830184620007ff565b949350505050565b5f602082019050620008605f830184620007ff565b92915050565b610dfc80620008745f395ff3fe608060405234801561000f575f80fd5b5060043610610091575f3560e01c8063313ce56711610064578063313ce5671461013157806370a082311461014f57806395d89b411461017f578063a9059cbb1461019d578063dd62ed3e146101cd57610091565b806306fdde0314610095578063095ea7b3146100b357806318160ddd146100e357806323b872dd14610101575b5f80fd5b61009d6101fd565b6040516100aa9190610a75565b60405180910390f35b6100cd60048036038101906100c89190610b26565b61028d565b6040516100da9190610b7e565b60405180910390f35b6100eb6102af565b6040516100f89190610ba6565b60405180910390f35b61011b60048036038101906101169190610bbf565b6102b8565b6040516101289190610b7e565b60405180910390f35b6101396102e6565b6040516101469190610c2a565b60405180910390f35b61016960048036038101906101649190610c43565b6102ee565b6040516101769190610ba6565b60405180910390f35b610187610333565b6040516101949190610a75565b60405180910390f35b6101b760048036038101906101b29190610b26565b6103c3565b6040516101c49190610b7e565b60405180910390f35b6101e760048036038101906101e29190610c6e565b6103e5565b6040516101f49190610ba6565b60405180910390f35b60606003805461020c90610cd9565b80601f016020809104026020016040519081016040528092919081815260200182805461023890610cd9565b80156102835780601f1061025a57610100808354040283529160200191610283565b820191905f5260205f20905b81548152906001019060200180831161026657829003601f168201915b5050505050905090565b5f80610297610467565b90506102a481858561046e565b600191505092915050565b5f600254905090565b5f806102c2610467565b90506102cf858285610480565b6102da858585610513565b60019150509392505050565b5f6012905090565b5f805f8373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020015f20549050919050565b60606004805461034290610cd9565b80601f016020809104026020016040519081016040528092919081815260200182805461036e90610cd9565b80156103b95780601f10610390576101008083540402835291602001916103b9565b820191905f5260205f20905b81548152906001019060200180831161039c57829003601f168201915b5050505050905090565b5f806103cd610467565b90506103da818585610513565b600191505092915050565b5f60015f8473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020015f205f8373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020015f2054905092915050565b5f33905090565b61047b8383836001610603565b505050565b5f61048b84846103e5565b90507fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff81101561050d57818110156104fe578281836040517ffb8f41b20000000000000000000000000000000000000000000000000000000081526004016104f593929190610d18565b60405180910390fd5b61050c84848484035f610603565b5b50505050565b5f73ffffffffffffffffffffffffffffffffffffffff168373ffffffffffffffffffffffffffffffffffffffff1603610583575f6040517f96c6fd1e00000000000000000000000000000000000000000000000000000000815260040161057a9190610d4d565b60405180910390fd5b5f73ffffffffffffffffffffffffffffffffffffffff168273ffffffffffffffffffffffffffffffffffffffff16036105f3575f6040517fec442f050000000000000000000000000000000000000000000000000000000081526004016105ea9190610d4d565b60405180910390fd5b6105fe8383836107d2565b505050565b5f73ffffffffffffffffffffffffffffffffffffffff168473ffffffffffffffffffffffffffffffffffffffff1603610673575f6040517fe602df0500000000000000000000000000000000000000000000000000000000815260040161066a9190610d4d565b60405180910390fd5b5f73ffffffffffffffffffffffffffffffffffffffff168373ffffffffffffffffffffffffffffffffffffffff16036106e3575f6040517f94280d620000000000000000000000000000000000000000000000000000000081526004016106da9190610d4d565b60405180910390fd5b8160015f8673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020015f205f8573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020015f208190555080156107cc578273ffffffffffffffffffffffffffffffffffffffff168473ffffffffffffffffffffffffffffffffffffffff167f8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b925846040516107c39190610ba6565b60405180910390a35b50505050565b5f73ffffffffffffffffffffffffffffffffffffffff168373ffffffffffffffffffffffffffffffffffffffff1603610822578060025f8282546108169190610d93565b925050819055506108f0565b5f805f8573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020015f20549050818110156108ab578381836040517fe450d38c0000000000000000000000000000000000000000000000000000000081526004016108a293929190610d18565b60405180910390fd5b8181035f808673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020015f2081905550505b5f73ffffffffffffffffffffffffffffffffffffffff168273ffffffffffffffffffffffffffffffffffffffff1603610937578060025f8282540392505081905550610981565b805f808473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020015f205f82825401925050819055505b8173ffffffffffffffffffffffffffffffffffffffff168373ffffffffffffffffffffffffffffffffffffffff167fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef836040516109de9190610ba6565b60405180910390a3505050565b5f81519050919050565b5f82825260208201905092915050565b5f5b83811015610a22578082015181840152602081019050610a07565b5f8484015250505050565b5f601f19601f8301169050919050565b5f610a47826109eb565b610a5181856109f5565b9350610a61818560208601610a05565b610a6a81610a2d565b840191505092915050565b5f6020820190508181035f830152610a8d8184610a3d565b905092915050565b5f80fd5b5f73ffffffffffffffffffffffffffffffffffffffff82169050919050565b5f610ac282610a99565b9050919050565b610ad281610ab8565b8114610adc575f80fd5b50565b5f81359050610aed81610ac9565b92915050565b5f819050919050565b610b0581610af3565b8114610b0f575f80fd5b50565b5f81359050610b2081610afc565b92915050565b5f8060408385031215610b3c57610b3b610a95565b5b5f610b4985828601610adf565b9250506020610b5a85828601610b12565b9150509250929050565b5f8115159050919050565b610b7881610b64565b82525050565b5f602082019050610b915f830184610b6f565b92915050565b610ba081610af3565b82525050565b5f602082019050610bb95f830184610b97565b92915050565b5f805f60608486031215610bd657610bd5610a95565b5b5f610be386828701610adf565b9350506020610bf486828701610adf565b9250506040610c0586828701610b12565b9150509250925092565b5f60ff82169050919050565b610c2481610c0f565b82525050565b5f602082019050610c3d5f830184610c1b565b92915050565b5f60208284031215610c5857610c57610a95565b5b5f610c6584828501610adf565b91505092915050565b5f8060408385031215610c8457610c83610a95565b5b5f610c9185828601610adf565b9250506020610ca285828601610adf565b9150509250929050565b7f4e487b71000000000000000000000000000000000000000000000000000000005f52602260045260245ffd5b5f6002820490506001821680610cf057607f821691505b602082108103610d0357610d02610cac565b5b50919050565b610d1281610ab8565b82525050565b5f606082019050610d2b5f830186610d09565b610d386020830185610b97565b610d456040830184610b97565b949350505050565b5f602082019050610d605f830184610d09565b92915050565b7f4e487b71000000000000000000000000000000000000000000000000000000005f52601160045260245ffd5b5f610d9d82610af3565b9150610da883610af3565b9250828201905080821115610dc057610dbf610d66565b5b9291505056fea2646970667358221220cbf3265ba91575b335d6a50312bcfe7dbc90dcddf9f8ad30d00f81bb265bb67064736f6c63430008140033";

    public static final String FUNC_NAME = "name";

    public static final String FUNC_APPROVE = "approve";

    public static final String FUNC_TOTALSUPPLY = "totalSupply";

    public static final String FUNC_TRANSFERFROM = "transferFrom";

    public static final String FUNC_DECIMALS = "decimals";

    public static final String FUNC_BURN = "burn";

    public static final String FUNC_BALANCEOF = "balanceOf";

    public static final String FUNC_BURNFROM = "burnFrom";

    public static final String FUNC_SYMBOL = "symbol";

    public static final String FUNC_TRANSFER = "transfer";

    public static final String FUNC_APPROVEANDCALL = "approveAndCall";

    public static final String FUNC_ALLOWANCE = "allowance";

    public static final Event TRANSFER_EVENT = new Event("Transfer",
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {},new TypeReference<Uint256>(false) {}));
    ;

    public static final Event BURN_EVENT = new Event("Burn",
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {},new TypeReference<Uint256>(false) {}));
    ;

    protected TokenERC20(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected TokenERC20(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public RemoteCall<String> name() {
        final Function function = new Function(FUNC_NAME,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<TransactionReceipt> approve(String _spender, BigInteger _value) {
        final Function function = new Function(
                FUNC_APPROVE,
                Arrays.<Type>asList(new Address(_spender),
                        new Uint256(_value)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<BigInteger> totalSupply() {
        final Function function = new Function(FUNC_TOTALSUPPLY,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<TransactionReceipt> transferFrom(String _from, String _to, BigInteger _value) {
        final Function function = new Function(
                FUNC_TRANSFERFROM,
                Arrays.<Type>asList(new Address(_from),
                        new Address(_to),
                        new Uint256(_value)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<BigInteger> decimals() {
        final Function function = new Function(FUNC_DECIMALS,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint8>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<TransactionReceipt> burn(BigInteger _value) {
        final Function function = new Function(
                FUNC_BURN,
                Arrays.<Type>asList(new Uint256(_value)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<BigInteger> balanceOf(String param0) {
        final Function function = new Function(FUNC_BALANCEOF,
                Arrays.<Type>asList(new Address(param0)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<TransactionReceipt> burnFrom(String _from, BigInteger _value) {
        final Function function = new Function(
                FUNC_BURNFROM,
                Arrays.<Type>asList(new Address(_from),
                        new Uint256(_value)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<String> symbol() {
        final Function function = new Function(FUNC_SYMBOL,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<TransactionReceipt> transfer(String _to, BigInteger _value) {
        final Function function = new Function(
                FUNC_TRANSFER,
                Arrays.<Type>asList(new Address(_to),
                        new Uint256(_value)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> approveAndCall(String _spender, BigInteger _value, byte[] _extraData) {
        final Function function = new Function(
                FUNC_APPROVEANDCALL,
                Arrays.<Type>asList(new Address(_spender),
                        new Uint256(_value),
                        new org.web3j.abi.datatypes.DynamicBytes(_extraData)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<BigInteger> allowance(String param0, String param1) {
        final Function function = new Function(FUNC_ALLOWANCE,
                Arrays.<Type>asList(new Address(param0),
                        new Address(param1)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public static RemoteCall<TokenERC20> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit, BigInteger initialSupply, String tokenName, String tokenSymbol) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new Uint256(initialSupply),
                new Utf8String(tokenName),
                new Utf8String(tokenSymbol)));
        return deployRemoteCall(TokenERC20.class, web3j, credentials, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    public static RemoteCall<TokenERC20> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit, BigInteger initialSupply, String tokenName, String tokenSymbol) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new Uint256(initialSupply),
                new Utf8String(tokenName),
                new Utf8String(tokenSymbol)));
        return deployRemoteCall(TokenERC20.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    public List<TransferEventResponse> getTransferEvents(TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList = extractEventParametersWithLog(TRANSFER_EVENT, transactionReceipt);
        ArrayList<TransferEventResponse> responses = new ArrayList<TransferEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            TransferEventResponse typedResponse = new TransferEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.from = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.to = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.value = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    //    public Observable<TransferEventResponse> transferEventObservable(EthFilter filter) {
    //        return web3j.ethLogObservable(filter).map(new Func1<Log, TransferEventResponse>() {
    //            @Override
    //            public TransferEventResponse call(Log log) {
    //                EventValuesWithLog eventValues = extractEventParametersWithLog(TRANSFER_EVENT, log);
    //                TransferEventResponse typedResponse = new TransferEventResponse();
    //                typedResponse.log = log;
    //                typedResponse.from = (String) eventValues.getIndexedValues().get(0).getValue();
    //                typedResponse.to = (String) eventValues.getIndexedValues().get(1).getValue();
    //                typedResponse.value = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
    //                return typedResponse;
    //            }
    //        });
    //    }
    //
    //    public Observable<TransferEventResponse> transferEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
    //        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
    //        filter.addSingleTopic(EventEncoder.encode(TRANSFER_EVENT));
    //        return transferEventObservable(filter);
    //    }

    public List<BurnEventResponse> getBurnEvents(TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList = extractEventParametersWithLog(BURN_EVENT, transactionReceipt);
        ArrayList<BurnEventResponse> responses = new ArrayList<BurnEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            BurnEventResponse typedResponse = new BurnEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.from = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.value = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    //    public Observable<BurnEventResponse> burnEventObservable(EthFilter filter) {
    //        return web3j.ethLogObservable(filter).map(new Func1<Log, BurnEventResponse>() {
    //            @Override
    //            public BurnEventResponse call(Log log) {
    //                EventValuesWithLog eventValues = extractEventParametersWithLog(BURN_EVENT, log);
    //                BurnEventResponse typedResponse = new BurnEventResponse();
    //                typedResponse.log = log;
    //                typedResponse.from = (String) eventValues.getIndexedValues().get(0).getValue();
    //                typedResponse.value = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
    //                return typedResponse;
    //            }
    //        });
    //    }
    //
    //    public Observable<BurnEventResponse> burnEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
    //        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
    //        filter.addSingleTopic(EventEncoder.encode(BURN_EVENT));
    //        return burnEventObservable(filter);
    //    }

    public static TokenERC20 load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new TokenERC20(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    public static TokenERC20 load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new TokenERC20(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static class TransferEventResponse {
        public Log log;

        public String from;

        public String to;

        public BigInteger value;
    }

    public static class BurnEventResponse {
        public Log log;

        public String from;

        public BigInteger value;
    }
}
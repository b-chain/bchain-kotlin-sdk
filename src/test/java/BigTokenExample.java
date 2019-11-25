import kotlin.Pair;
import org.bchain.node.BigTokenHelper;
import org.bchain.node.Node;
import org.bchain.node.model.ActionInfo;
import org.bchain.node.model.ArgumentInfo;
import org.bchain.node.model.BlockInfo;
import org.bchain.node.model.TransactionInfo;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.List;

/**
 * BigToken
 *
 * @author lihang
 * @since 2019/11/9
 */
public class BigTokenExample {

    private Node node;
    private BigTokenHelper bigTokenHelper;

    @Before
    public void init() {
        String apiUrl = "127.0.0.1"; // BC Chain Node API URL
        int port = 8989; // BC Chain Node API URL PORT Default 8989
        String privateKey = "";
        this.node = new Node(apiUrl, port, privateKey, false);
        bigTokenHelper = new BigTokenHelper("0x783719d53232f53570a63ea41da4be1669b4a2fd", node);
    }


    //获取合约账户余额
    @Test
    public void getBalance() {
        String address = "0xb3726e0fed2b7eaaa609b77ec507f26d5f8fe561";
        Pair<BigDecimal, Integer> balanceOf = bigTokenHelper.balanceOf("test", address);
        System.out.println("test balance: " + balanceOf.component1().stripTrailingZeros().toPlainString());
    }


    //发送交易
    @Test
    public void multipleTransferBc() {
        int decimal = bigTokenHelper.getDecimal("test");

        // 发起账户 且要和new Node的私钥对应的地址相同
        String fromAddress = "0xb3726e0fed2b7eaaa609b77ec507f26d5f8fe561";
        String toAddress = "0xb3726e0fed2b7eaaa609b77ec507f26d5f8fe561";
        BigDecimal amount = new BigDecimal("100");


        long blockTopNumber = node.getBlockTopNumber();
        long nonce = node.getAccountNonce(fromAddress, blockTopNumber);

        String txHash = bigTokenHelper.multipleTransfer(new BigTokenHelper.BigTokenTransferParameter[]{
                new BigTokenHelper.BigTokenTransferParameter(toAddress, "test", amount, "")
        }, BigDecimal.ZERO, 100, nonce, BigInteger.ONE);

        System.out.println("tx hash = " + txHash);

    }


    //扫区块数据
    @Test
    public void getBlockByNumber() throws Exception {
        long blockTopNumber = node.getBlockTopNumber();
        BlockInfo blockInfo = node.getBlockByNumber(blockTopNumber, true);

        for (TransactionInfo transaction : blockInfo.getTransactions()) {
            for (ActionInfo action : transaction.getActions()) {
                if (!"0x783719d53232f53570a63ea41da4be1669b4a2fd".equalsIgnoreCase(action.getContract())) {
                    continue;
                }

                List<ArgumentInfo> arguments = action.getParameter().getArguments();
                if (arguments.size() > 1) {
                    byte[] balanceBytes = arguments.get(1).getValueBytes();
                    String bigAmountValue = new String(balanceBytes, "UTF-8").trim();
                    BigDecimal amount = new BigDecimal(bigAmountValue).divide(BigDecimal.TEN.pow(bigTokenHelper.getDecimal("test")), 8, RoundingMode.DOWN);
                    // 得到合约的交易信息


                }
            }
        }
    }

}

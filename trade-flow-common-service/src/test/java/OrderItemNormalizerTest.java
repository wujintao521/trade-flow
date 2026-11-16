import com.trade.common.exception.BusinessException;
import com.trade.request.order.OrderItemRequest;

import com.trade.service.order.OrderItemNormalizer;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderItemNormalizerTest {

    private final OrderItemNormalizer normalizer = new OrderItemNormalizer();

    @Test
    void mergesDuplicateSkusAndSortsResults() {

        List<OrderItemRequest> input = List.of(
                new OrderItemRequest(1002L, 1),
                new OrderItemRequest(1001L, 2),
                new OrderItemRequest(1001L, 3));

        List<OrderItemRequest> result = normalizer.normalize(input);
        assertEquals(
                List.of(
                        new OrderItemRequest(1001L, 5),
                        new OrderItemRequest(1002L, 1)),
                result);

        // 输入列表没有被修改。
        assertEquals(3, input.size());
    }

    @Test
    void rejectsEmptyInput() {

        assertInvalid(null);
        assertInvalid(List.of());
    }

    @Test
    void rejectsInvalidSku() {

        assertInvalid(Collections.singletonList(null));

        assertInvalid(List.of(new OrderItemRequest(null, 1)));

        assertInvalid(List.of(new OrderItemRequest(0L, 1)));

        assertInvalid(List.of(new OrderItemRequest(-1L, 1)));
    }

    @Test
    void rejectsInvalidQuantity() {

        assertInvalid(List.of(
                new OrderItemRequest(1001L, null)));

        assertInvalid(List.of(
                new OrderItemRequest(1001L, 0)));

        assertInvalid(List.of(
                new OrderItemRequest(1001L, -1)));

        assertInvalid(List.of(
                new OrderItemRequest(1001L, Integer.MAX_VALUE)));
    }

    @Test
    void rejectsMergedQuantityOverflow() {

        assertInvalid(List.of(new OrderItemRequest(1001L, 9999), new OrderItemRequest(1001L, 1)));
    }

    @Test
    void rejectsTooManyRawItems() {

        List<OrderItemRequest> input =
                Collections.nCopies(
                        101,
                        new OrderItemRequest(1001L, 1));

        assertInvalid(input);
    }

    @Test
    void rejectsTooManyDistinctSkus() {

        List<OrderItemRequest> input = new ArrayList<>();

        for (long skuId = 1; skuId <= 51; skuId++) {
            input.add(new OrderItemRequest(skuId, 1));
        }

        assertInvalid(input);
    }

    @Test
    void returnedSnapshotIsImmutable() {

        List<OrderItemRequest> input = new ArrayList<>();
        input.add(new OrderItemRequest(1001L, 2));

        List<OrderItemRequest> result =
                normalizer.normalize(input);

        input.clear();

        assertEquals(
                List.of(new OrderItemRequest(1001L, 2)),
                result);

        assertThrows(
                UnsupportedOperationException.class,
                result::clear);
    }

    @Test
    void acceptsQuantityUpperBoundary() {

        List<OrderItemRequest> result =
                normalizer.normalize(
                        List.of(
                                new OrderItemRequest(1001L, 9999)));

        assertEquals(
                List.of(new OrderItemRequest(1001L, 9999)),
                result);
    }

    private void assertInvalid(List<OrderItemRequest> input) {
        BusinessException exception = assertThrows(BusinessException.class, () -> normalizer.normalize(input));
        assertEquals(40001, exception.getCode());
        System.out.printf("输入：%s；异常：%s；错误码：%d；原因：%s%n", input, exception.getClass().getSimpleName(), exception.getCode(), exception.getMessage());
    }
}
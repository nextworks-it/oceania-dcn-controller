package it.nextworks.nephele.OFTranslator;

import it.nextworks.nephele.OFAAService.NetSolBase;
import it.nextworks.nephele.OFAAService.ODLInventory.Const;
import it.nextworks.nephele.appaffdb.DbManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@CrossOrigin
@RestController
public class TranslatorController {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    DbManager dbManager;

    @RequestMapping(value = "/translate", method = RequestMethod.POST)
    @ApiOperation(value = "postNetAllocMatrix",
        nickname = "Post network alocation matrix to be translated")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success", response = Inventory.class)})
    public Inventory makeinventory(
        @RequestBody NetSolBase networkAllocationSolution) {
        switch (networkAllocationSolution.method) {
            case "INCREMENTAL":
                Const.update(networkAllocationSolution.getResult());
                break;
            case "FULL":
                Const.init(networkAllocationSolution.getResult());
                break;
            default:
                throw new IllegalArgumentException(String.format(
                        "Unexpected method %s",
                        networkAllocationSolution.method
                ));
        }
        NetworkBuilder net = new NetworkBuilder(dbManager);
        return net.build();
    }

    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleAppException(NullPointerException ex) {
        log.info(ex.getMessage(), ex);
        return ex.getMessage();
    }

}

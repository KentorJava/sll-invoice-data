/**
 *  Copyright (c) 2013 SLL <http://sll.se/>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package se.sll.invoicedata.core.service.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import se.sll.invoicedata.core.model.entity.BusinessEventEntity;
import se.sll.invoicedata.core.model.entity.ItemEntity;
import se.sll.invoicedata.core.model.repository.BusinessEventRepository;
import se.sll.invoicedata.core.service.InvoiceDataErrorCodeEnum;
import se.sll.invoicedata.core.service.InvoiceDataService;
import se.sll.invoicedata.core.service.RatingService;


@Service
@Transactional
public class InvoiceDataServiceImpl implements InvoiceDataService {

    @Autowired
    private BusinessEventRepository businessEventRepository;
    
    @Autowired
    private RatingService ratingService;

    
    @Override
    public void registerBusinessEvent(BusinessEventEntity businessEventEntity) {
        businessEventRepository.save(rate(validate(businessEventEntity)));
    }

    @Override
    public BusinessEventEntity getBusinessEvent(String eventId) {
        return businessEventRepository.findOne(eventId);
    }

    @Override
    public List<BusinessEventEntity> getAllUnprocessedBusinessEvents(
            String supplierId) {
        return businessEventRepository.findBySupplierIdAndPendingIsTrue(supplierId);
    }
    
    
    //
    private static void mandatory(final String s, final String field) {
        if (s == null || s.length() ==  0) {
            throw InvoiceDataErrorCodeEnum.VALIDATION_ERROR.createException(field);            
        }
    }

    //
    private static void mandatory(final Object s, final String field) {
        if (s == null) {
            throw InvoiceDataErrorCodeEnum.VALIDATION_ERROR.createException(field);            
        }
    }

    /**
     * Rates all items of a {@link BusinessEventEntity}
     * 
     * @param businessEventEntity the business event.
     * @return the rated business event, i.e. price has been set to all items.
     */
    protected BusinessEventEntity rate(BusinessEventEntity businessEventEntity) {
        for (ItemEntity itemEntity : validate(businessEventEntity).getItemEntities()) {
            itemEntity.setPrice(ratingService.rate(itemEntity));
        }
        return businessEventEntity;
    }

    /**
     * Validates business entity.
     * 
     * @param businessEventEntity the entity.
     * @return the same entity reference as passed as argument.
     */
    protected BusinessEventEntity validate(final BusinessEventEntity businessEventEntity) {
        
        // mandatory fields according to schema
        mandatory(businessEventEntity.getServiceCode(), "event.serviceCode");
        mandatory(businessEventEntity.getSupplierId(), "event.supplierId");
        mandatory(businessEventEntity.getSupplierName(), "event.supplierName");
        mandatory(businessEventEntity.getSignedBy(), "event.signedBy");
        mandatory(businessEventEntity.getSignedTimestamp(), "event.signedTimestamp");
        mandatory(businessEventEntity.getStartTimestamp(), "event.startTimestamp");
        mandatory(businessEventEntity.getEndTimestamp(), "event.endTimestamp");

        // valid time period
        if (businessEventEntity.getEndTimestamp().before(businessEventEntity.getStartTimestamp())) {
            throw InvoiceDataErrorCodeEnum.VALIDATION_ERROR.createException("event.endTimestamp is before event.startTimestamp");            
        }
        
        // mandatory fields according to schema
        final List<ItemEntity> items = businessEventEntity.getItemEntities();
        if (items.size() == 0) {
            throw InvoiceDataErrorCodeEnum.VALIDATION_ERROR.createException("event.items");            
        }

        // items
        for (final ItemEntity itemEntity : items) {
            mandatory(itemEntity.getDescription(), "item.description");
            mandatory(itemEntity.getItemId(), "item.id");
            mandatory(itemEntity.getEvent(), "item.event");
            final BigDecimal qty = itemEntity.getQty();
            if (qty.floatValue() < 0f || qty.floatValue() > 99999f) {
                throw InvoiceDataErrorCodeEnum.VALIDATION_ERROR.createException("item.qty, out of range: " + qty.floatValue());
            }
            if (qty.scale() > 2) {
                throw InvoiceDataErrorCodeEnum.VALIDATION_ERROR.createException("item.qty, invalid scale: " + qty.floatValue());                
            }
        }
        
        return businessEventEntity;
    }

}

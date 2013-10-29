/**
 * Copyright (c) 2013 SLL. <http://sll.se>
 *
 * This file is part of Invoice-Data.
 *
 *     Invoice-Data is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Invoice-Data is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with Invoice-Data.  If not, see <http://www.gnu.org/licenses/lgpl.txt>.
 */

/**
 * 
 */
package se.sll.invoicedata.core.service.impl;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import riv.sll.invoicedata._1.Event;
import riv.sll.invoicedata._1.InvoiceData;
import riv.sll.invoicedata._1.InvoiceDataHeader;
import riv.sll.invoicedata._1.RegisteredEvent;
import riv.sll.invoicedata.createinvoicedataresponder._1.CreateInvoiceDataRequest;
import riv.sll.invoicedata.getinvoicedataresponder._1.GetInvoiceDataRequest;
import riv.sll.invoicedata.listinvoicedataresponder._1.ListInvoiceDataRequest;
import se.sll.invoicedata.core.service.InvoiceDataService;
import se.sll.invoicedata.core.service.InvoiceDataServiceException;
import se.sll.invoicedata.core.support.TestSupport;

/**
 * @author muqkha
 * 
 */
public class InvoiceDataServiceImplTest extends TestSupport {

	@Autowired
	private InvoiceDataService invoiceDataService;

	@Test
	@Transactional
	@Rollback(true)
	public void testFind_BusinessEvent_By_Id() {

		final Event e = createSampleEvent();
		e.getItemList().get(0).setPrice(null);
		invoiceDataService.registerEvent(e);
		
		GetInvoiceDataRequest getIDRequest = new GetInvoiceDataRequest();
		getIDRequest.setSupplierId(e.getSupplierId());
		getIDRequest.setPaymentResponsible(e.getPaymentResponsible());
		final List<RegisteredEvent> l = invoiceDataService.getAllUnprocessedBusinessEvents(getIDRequest);
		final RegisteredEvent f = l.get(0);

		assertEquals(e.getEventId(), f.getEventId());
		assertEquals(e.getSupplierName(), f.getSupplierName());
		assertEquals(e.getAcknowledgedBy(), f.getAcknowledgedBy());
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void testRegister_Multiple_Events() {
		final Event e = createSampleEvent();
		e.getItemList().get(0).setPrice(null);
		invoiceDataService.registerEvent(e);
		e.setAcknowledgementId(UUID.randomUUID().toString());
		invoiceDataService.registerEvent(e);
		
		GetInvoiceDataRequest getIDRequest = new GetInvoiceDataRequest();
		getIDRequest.setSupplierId(e.getSupplierId());
		getIDRequest.setPaymentResponsible(e.getPaymentResponsible());
		final List<RegisteredEvent> l = invoiceDataService.getAllUnprocessedBusinessEvents(getIDRequest);
		final RegisteredEvent f = l.get(0);

		assertEquals(e.getEventId(), f.getEventId());
		assertEquals(e.getSupplierName(), f.getSupplierName());
		assertEquals(e.getAcknowledgedBy(), f.getAcknowledgedBy());

	}
		
	@Test (expected = InvoiceDataServiceException.class)
	@Transactional
	@Rollback(true)
	public void testRegisterEvent_With_No_Items_Result_Fail() {
		final Event e = createSampleEvent();
		e.setItemList(null);
		invoiceDataService.registerEvent(e);		
	}
	
	@Test (expected = InvoiceDataServiceException.class)
	@Transactional
	@Rollback(true)
	public void testRegisterEvent_With_Invalid_Item_Qty_Result_Fail() {
		final Event e = createSampleEvent();
		e.getItemList().get(0).setQty(new BigDecimal(9999999));
		invoiceDataService.registerEvent(e);		
	}
	
	private void registerEvents(String supplierId, List<String> ids) {
		for (final String id : ids) {
			final Event e = createSampleEvent();
			e.setEventId(id);
			e.setSupplierId(supplierId);

			invoiceDataService.registerEvent(e);
		}
	}

	private CreateInvoiceDataRequest createInvoiceData(String supplierId) {
		final List<String> ids = Arrays.asList(new String[] { "event-1",
				"event-2", "event-3" });

		registerEvents(supplierId, ids);

		final CreateInvoiceDataRequest ie = new CreateInvoiceDataRequest();
		ie.setSupplierId(supplierId);
		ie.setPaymentResponsible("HSF");
		ie.setCreatedBy("test-auto");
		
		GetInvoiceDataRequest getIDRequest = new GetInvoiceDataRequest();
		getIDRequest.setSupplierId(supplierId);
		
		final List<RegisteredEvent> l = invoiceDataService
				.getAllUnprocessedBusinessEvents(getIDRequest);

		for (final RegisteredEvent e : l) {
			ie.getAcknowledgementIdList().add(e.getAcknowledgementId());
		}

		invoiceDataService.createInvoiceData(ie);

		return ie;
	}
	
	@Test (expected = InvoiceDataServiceException.class)
	@Transactional
	@Rollback(true)
	public void testCreateInvoiceData_Result_Fail() {
		final CreateInvoiceDataRequest ie = new CreateInvoiceDataRequest();
		invoiceDataService.createInvoiceData(ie);
	}
	 
	@Test (expected = InvoiceDataServiceException.class)
	@Transactional
	@Rollback(true)
	public void testCreateInvoiceData_Invalid_Supplier() {
		final Event e = createSampleEvent();
		invoiceDataService.registerEvent(e);
		
		final CreateInvoiceDataRequest createReq = new CreateInvoiceDataRequest();
		createReq.setSupplierId(e.getSupplierId() + "_invalid");
		createReq.setPaymentResponsible(e.getPaymentResponsible());
		createReq.setCreatedBy("testCreateInvoiceData_Invalid_Supplier");
		createReq.getAcknowledgementIdList().add(e.getAcknowledgementId());
		invoiceDataService.createInvoiceData(createReq);
	}
	
	@Test (expected = InvoiceDataServiceException.class)
	@Transactional
	@Rollback(true)
	public void testCreateInvoiceData_Invalid_PaymentResponsible() {
		final Event e = createSampleEvent();
		invoiceDataService.registerEvent(e);
		
		final CreateInvoiceDataRequest createReq = new CreateInvoiceDataRequest();
		createReq.setSupplierId(e.getSupplierId());
		createReq.setPaymentResponsible(e.getPaymentResponsible() + "_invalid");
		createReq.setCreatedBy("testCreateInvoiceData_Invalid_PaymentResponsible");
		createReq.getAcknowledgementIdList().add(e.getAcknowledgementId());
		invoiceDataService.createInvoiceData(createReq);
	}
	
	@Test (expected = InvoiceDataServiceException.class)
	@Transactional
	@Rollback(true)
	public void testCreateInvoiceData_Duplicate_AckList() {
		final Event e = createSampleEvent();
		invoiceDataService.registerEvent(e);
		
		final CreateInvoiceDataRequest createReq = new CreateInvoiceDataRequest();
		createReq.setSupplierId(e.getSupplierId());
		createReq.setPaymentResponsible(e.getPaymentResponsible());
		createReq.setCreatedBy("testCreateInvoiceData_Duplicate_AckList");
		createReq.getAcknowledgementIdList().add(e.getAcknowledgementId());
		createReq.getAcknowledgementIdList().add(e.getAcknowledgementId());
		invoiceDataService.createInvoiceData(createReq);
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void testRegisterInvoiceData_From_Pending_And_Credit() {
		final String supplierId = "test-supplier-45";
		final CreateInvoiceDataRequest ie = createInvoiceData(supplierId);

		registerEvents(supplierId,
				Arrays.asList(new String[] { "event-1", "event-2", "event-3" }));
		
		GetInvoiceDataRequest getIDRequest = new GetInvoiceDataRequest();
		getIDRequest.setSupplierId(supplierId);
		
		final List<RegisteredEvent> l = invoiceDataService
				.getAllUnprocessedBusinessEvents(getIDRequest);
		// one credit event shall be created for each new
		assertEquals(ie.getAcknowledgementIdList().size() * 2, l.size());

		int credits = 0;
		for (final RegisteredEvent e : l) {
			if (e.isCredit()) {
				credits++;
			}
		}
		assertEquals(3, credits);
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testGetAllUnprocessedBusinessEvents() {

		final Event e = createSampleEvent();

		invoiceDataService.registerEvent(e);
		
		GetInvoiceDataRequest getIDRequest = new GetInvoiceDataRequest();
		getIDRequest.setSupplierId(e.getSupplierId());
		getIDRequest.setPaymentResponsible(e.getPaymentResponsible());
		
		final List<RegisteredEvent> regEventList = invoiceDataService
				.getAllUnprocessedBusinessEvents(getIDRequest);

		assertNotNull(regEventList);
		assertEquals(1, regEventList.size());
		assertEquals(e.getSupplierName(), regEventList.get(0).getSupplierName());
		assertEquals(e.getAcknowledgedBy(), regEventList.get(0)
				.getAcknowledgedBy());
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void testListAllInvoiceData_With_Alternatives() {

		final Event e = createSampleEvent();
		invoiceDataService.registerEvent(e);
		
		final CreateInvoiceDataRequest createReq = new CreateInvoiceDataRequest();
		createReq.setSupplierId(e.getSupplierId());
		createReq.setPaymentResponsible(e.getPaymentResponsible());
		createReq.setCreatedBy("testListAllInvoiceData_With_Supplier_ID");
		createReq.getAcknowledgementIdList().add(e.getAcknowledgementId());
		invoiceDataService.createInvoiceData(createReq);

		// Request with only supplier id
		ListInvoiceDataRequest invoiceListRequest = new ListInvoiceDataRequest();
		invoiceListRequest.setSupplierId(e.getSupplierId());

		List<InvoiceDataHeader> invoiceDataList = invoiceDataService
				.listAllInvoiceData(invoiceListRequest);

		assertNotNull(invoiceDataList);
		assertEquals(e.getPaymentResponsible(), invoiceDataList.get(0)
				.getPaymentResponsible());
		
		// Request with only payment responsible
		invoiceListRequest = new ListInvoiceDataRequest();
		invoiceListRequest.setPaymentResponsible(e.getPaymentResponsible());

		invoiceDataList = invoiceDataService
				.listAllInvoiceData(invoiceListRequest);

		assertNotNull(invoiceDataList);
		assertEquals(e.getSupplierId(), invoiceDataList.get(0)
				.getSupplierId());
		
		// Request with supplierId and paymentResponsible
		invoiceListRequest = new ListInvoiceDataRequest();
		invoiceListRequest.setSupplierId(e.getSupplierId());
		invoiceListRequest.setPaymentResponsible(e.getPaymentResponsible());
		
		invoiceDataList = invoiceDataService
				.listAllInvoiceData(invoiceListRequest);

		assertNotNull(invoiceDataList);
		assertEquals(e.getSupplierId(), invoiceDataList.get(0)
				.getSupplierId());
		assertEquals(e.getPaymentResponsible(), invoiceDataList.get(0)
				.getPaymentResponsible());
		
	}
	
	@Test (expected=InvoiceDataServiceException.class)
	@Transactional
	@Rollback(true)	
	public void testListAllInvoiceData_Result_Fail() {
		// Request with empty parameters
		ListInvoiceDataRequest invoiceListRequest = new ListInvoiceDataRequest();
		invoiceDataService.listAllInvoiceData(invoiceListRequest);
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void testGetInvoiceDataByReferenceId() {
		final Event e = createSampleEvent();
		invoiceDataService.registerEvent(e);
		
		final CreateInvoiceDataRequest createReq = new CreateInvoiceDataRequest();
		createReq.setSupplierId(e.getSupplierId());
		createReq.setPaymentResponsible(e.getPaymentResponsible());
		createReq.setCreatedBy("testGetInvoiceDataByReferenceId_With_Alternatives");
		createReq.getAcknowledgementIdList().add(e.getAcknowledgementId());
		String referenceId = invoiceDataService.createInvoiceData(createReq);
		
		InvoiceData invoiceData = invoiceDataService.getInvoiceDataByReferenceId(referenceId);
		
		assertNotNull(invoiceData);
		assertNotNull(invoiceData.getRegisteredEventList());
		assertNotNull(invoiceData.getRegisteredEventList().get(0).getItemList().get(0).getItemId());
	}
	
	@Test (expected = InvoiceDataServiceException.class)
	@Transactional
	@Rollback(true)
	public void testGetInvoiceDataBy_Dummy_ReferenceId() {
		//Valid referenceId
		String referenceId = "supplierId.00000";
		invoiceDataService.getInvoiceDataByReferenceId(referenceId);		
	}
	
	@Test (expected = InvoiceDataServiceException.class)
	@Transactional
	@Rollback(true)
	public void testGetInvoiceDataBy_Invalid_ReferenceId() {
		//Valid referenceId
		String referenceId = "supplierId.0000x";
		invoiceDataService.getInvoiceDataByReferenceId(referenceId);		
	}
	
	
	
}

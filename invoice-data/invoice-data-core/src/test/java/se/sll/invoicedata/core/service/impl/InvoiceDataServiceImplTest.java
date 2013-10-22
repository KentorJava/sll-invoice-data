/**
 * Copyright (c) 2013 SLL, <http://sll.se>
 *
 * This file is part of Invoice-Data.
 *
 *     Invoice Data is free software: you can redistribute it and/or modify
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

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import riv.sll.invoicedata._1.Event;
import riv.sll.invoicedata._1.InvoiceData;
import riv.sll.invoicedata._1.InvoiceDataHeader;
import riv.sll.invoicedata._1.Item;
import riv.sll.invoicedata._1.RegisteredEvent;
import riv.sll.invoicedata.createinvoicedataresponder._1.CreateInvoiceDataRequest;
import riv.sll.invoicedata.getinvoicedataresponder._1.GetInvoiceDataRequest;
import riv.sll.invoicedata.listinvoicedataresponder._1.ListInvoiceDataRequest;
import se.sll.invoicedata.core.model.entity.BusinessEventEntity;
import se.sll.invoicedata.core.service.InvoiceDataService;
import se.sll.invoicedata.core.support.TestSupport;

/**
 * @author muqkha
 * 
 */
public class InvoiceDataServiceImplTest extends TestSupport {

	@Autowired
	private InvoiceDataService invoiceDataService;

	protected Event createSampleEvent() {
		final BusinessEventEntity e = createSampleBusinessEventEntity();
		e.addItemEntity(createSampleItemEntity());

		final Event event = CoreUtil.copyProperties(e, Event.class);

		CoreUtil.copyGenericLists(event.getItemList(), e.getItemEntities(),
				Item.class);

		return event;
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testFind_BusinessEvent_By_Id() {

		final Event e = createSampleEvent();
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
	public void testGetAllInvoicedData() {
		final String supplierId = "test-supplier-all";

		createInvoiceData(supplierId);

		GetInvoiceDataRequest getIDRequest = new GetInvoiceDataRequest();
		getIDRequest.setSupplierId(supplierId);
		getIDRequest.setPaymentResponsible("HSF");

		final List<InvoiceDataHeader> invoiceDataList = invoiceDataService
				.getAllInvoicedData(getIDRequest);

		assertNotNull(invoiceDataList);
		assertEquals(1, invoiceDataList.size());
		assertEquals(supplierId, invoiceDataList.get(0).getSupplierId());
		assertEquals("HSF", invoiceDataList.get(0).getPaymentResponsible());
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testGetInvoiceData() {

		final Event e = createSampleEvent();
		invoiceDataService.registerEvent(e);
		e.setEventId("Event_2");
		e.setAcknowledgementId("Ack_123");
		invoiceDataService.registerEvent(e);
		
		GetInvoiceDataRequest getIDRequest = new GetInvoiceDataRequest();
		getIDRequest.setSupplierId(e.getSupplierId());
		getIDRequest.setPaymentResponsible(e.getPaymentResponsible());
		
		List<RegisteredEvent> regEvtList = invoiceDataService
				.getAllUnprocessedBusinessEvents(getIDRequest);

		final CreateInvoiceDataRequest createReq = new CreateInvoiceDataRequest();
		createReq.setSupplierId(regEvtList.get(0).getSupplierId());
		createReq.setPaymentResponsible(regEvtList.get(0)
				.getPaymentResponsible());
		createReq.setCreatedBy("test-auto");
		createReq.getAcknowledgementIdList().add(
				regEvtList.get(0).getAcknowledgementId());

		invoiceDataService.createInvoiceData(createReq);

		getIDRequest = new GetInvoiceDataRequest();
		getIDRequest.setSupplierId(e.getSupplierId());
		getIDRequest.setPaymentResponsible(e.getPaymentResponsible());
		List<InvoiceDataHeader> iDH = invoiceDataService
				.getAllInvoicedData(getIDRequest);
		
		assertNotNull(iDH);
		
		InvoiceData iData = invoiceDataService.getInvoiceDataByReferenceId(iDH
				.get(0).getReferenceId());

		assertNotNull(iData);
		assertEquals(e.getPaymentResponsible(), iData.getPaymentResponsible());
		assertEquals(e.getSupplierId(), iData.getSupplierId());
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testListAllInvoiceData_With_Supplier_ID() {

		final Event e = createSampleEvent();
		invoiceDataService.registerEvent(e);

		GetInvoiceDataRequest getIDRequest = new GetInvoiceDataRequest();
		getIDRequest.setSupplierId(e.getSupplierId());
		getIDRequest.setPaymentResponsible(e.getPaymentResponsible());
		
		List<RegisteredEvent> regEvtList = invoiceDataService
				.getAllUnprocessedBusinessEvents(getIDRequest);

		final CreateInvoiceDataRequest createReq = new CreateInvoiceDataRequest();
		createReq.setSupplierId(regEvtList.get(0).getSupplierId());
		createReq.setPaymentResponsible(regEvtList.get(0)
				.getPaymentResponsible());
		createReq.setCreatedBy("test-auto");
		createReq.getAcknowledgementIdList().add(
				regEvtList.get(0).getAcknowledgementId());

		invoiceDataService.createInvoiceData(createReq);

		// Request with only supplier id
		ListInvoiceDataRequest invoiceListRequest = new ListInvoiceDataRequest();
		invoiceListRequest.setSupplierId(e.getSupplierId());

		List<InvoiceDataHeader> invoiceDataList = invoiceDataService
				.listAllInvoiceData(invoiceListRequest);

		assertNotNull(invoiceDataList);
		assertEquals(e.getPaymentResponsible(), invoiceDataList.get(0)
				.getPaymentResponsible());
	}

}

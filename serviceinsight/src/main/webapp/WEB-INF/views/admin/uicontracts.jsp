<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<spring:message code="label_customer_title" var="lbl_cust"/>
<div>
    <div id="views_div" style="padding-bottom: 10px;">
        <span style="font-weight: bold;">Controller-less Views</span>
        <ul>
            <li>PATH <span style="color:coral;">/view/contracts/home</span> resolves to VIEW <span style="color:green;">contracts/home</span></li>
        </ul>
    </div>
    <div id="customer_div" style="padding-bottom: 10px;">
        <span style="font-weight: bold;">${lbl_cust}</span>
        <ul>
            <li>List All Customers: HTTP GET - /customers</li>
            <li>Search Customers: HTTP GET - /customers/search?s=</li>
            <li>Single Customer: HTTP GET - /customers/{id}</li>
            <li style="color:darkgrey;">CREATE Customer: HTTP POST - /customers</li>
            <li style="color:darkgrey;">UPDATE Customer: HTTP PUT - /customers</li>
            <li style="color:darkgrey;">DELETE Customer: HTTP DELETE - /customers/{id}</li>
        </ul>
        <p />
        <div id="uicustomer" style="margin-left: 10px;">
        </div>
    </div>
    <p />
    <div id="contract_div" style="padding-bottom: 10px;">
        <span style="font-weight: bold;">Contract</span>
        <ul>
            <li>List All Contracts: HTTP GET - /contracts</li>
            <li>List Customer Contract: HTTP GET - /contracts?cid=</li>
            <li>Single Contract: HTTP GET - /contracts/{id}</li>
            <li style="color:darkgrey;">CREATE Contract: HTTP POST - /contracts</li>
            <li style="color:darkgrey;">UPDATE Contract: HTTP PUT - /contracts</li>
            <li style="color:darkgrey;">DELETE Contract: HTTP DELETE - /contracts/{id}</li>
        </ul>
        <p />
        <div id="uicontract" style="margin-left: 10px;">
        </div>
    </div>
    <p />
    <div id="contractsvc_div" style="padding-bottom: 10px;">
        <span style="font-weight: bold;">Contract Service</span>
        <ul>
            <li>List Contract Services: HTTP GET - /services?cid=&startdate=&enddate=</li>
        </ul>
        <p />
        <div id="uicontractsvc" style="margin-left: 10px;">
        </div>
    </div>
    <spring:url value="/admin/sow.json" var="url_contract" />
    <spring:url value="/admin/customer.json" var="url_customer" />
    <spring:url value="/admin/contractservice.json" var="url_service" />
    <script type="text/javascript">
        $(document).ready(function() {
            $.ajax({
                'async': true,
                'url': '${url_contract}',
                'dataType': "json",
                'success': displayContract
            });
            $.ajax({
                'async': true,
                'url': '${url_customer}',
                'dataType': "json",
                'success': displayCustomer
            });
            $.ajax({
                'async': true,
                'url': '${url_service}',
                'dataType': "json",
                'success': displayService
            });
            return false;
        });
        function displayContract(data) {
            var apre = $('<pre>');
            apre.text(JSON.stringify(data, null, '\t'));
            apre.appendTo('#uicontract');
        }
        function displayCustomer(data) {
            var apre = $('<pre>');
            apre.text(JSON.stringify(data, null, '\t'));
            apre.appendTo('#uicustomer');
        }
        function displayService(data) {
            var apre = $('<pre>');
            apre.text(JSON.stringify(data, null, '\t'));
            apre.appendTo('#uicontractsvc');
        }
    </script>
</div>

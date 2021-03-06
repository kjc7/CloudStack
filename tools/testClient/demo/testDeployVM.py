#!/usr/bin/env python

from cloudstackTestCase import *

import unittest
import hashlib
import random

class TestDeployVm(cloudstackTestCase):
    """
    This test deploys a virtual machine into a user account 
    using the small service offering and builtin template
    """
    def setUp(self):
        """
        CloudStack internally saves its passwords in md5 form and that is how we
        specify it in the API. Python's hashlib library helps us to quickly hash
        strings as follows
        """
        mdf = hashlib.md5()
        mdf.update('password')
        mdf_pass = mdf.hexdigest()

        self.apiClient = self.testClient.getApiClient() #Get ourselves an API client

        self.acct = createAccount.createAccountCmd() #The createAccount command
        self.acct.accounttype = 0                    #We need a regular user. admins have accounttype=1
        self.acct.firstname = 'bugs'                 
        self.acct.lastname = 'bunny'                 #What's up doc?
        self.acct.password = mdf_pass                #The md5 hashed password string
        self.acct.username = 'bugs'
        self.acct.email = 'bugs@rabbithole.com'
        self.acct.account = 'bugs'
        self.acct.domainid = 1                       #The default ROOT domain
        self.acctResponse = self.apiClient.createAccount(self.acct)
        #And upon successful creation we'll log a helpful message in our logs
        self.debug("successfully created account: %s, user: %s, id: \
                   %s"%(self.acctResponse.account.account, \
                        self.acctResponse.account.username, \
                        self.acctResponse.account.id))

    def test_DeployVm(self):
        """
        Let's start by defining the attributes of our VM that we will be
        deploying on CloudStack. We will be assuming a single zone is available
        and is configured and all templates are Ready

        The hardcoded values are used only for brevity. 
        """
        deployVmCmd = deployVirtualMachine.deployVirtualMachineCmd()
        deployVmCmd.zoneid = 1
        deployVmCmd.account = self.acct.account
        deployVmCmd.domainid = self.acct.domainid
        deployVmCmd.templateid = 2
        deployVmCmd.serviceofferingid = 1

        deployVmResponse = self.apiClient.deployVirtualMachine(deployVmCmd)
        self.debug("VM %s was deployed in the job %s"%(deployVmResponse.id, deployVmResponse.jobid))

        # At this point our VM is expected to be Running. Let's find out what
        # listVirtualMachines tells us about VMs in this account

        listVmCmd = listVirtualMachines.listVirtualMachinesCmd()
        listVmCmd.id = deployVmResponse.id
        listVmResponse = self.apiClient.listVirtualMachines(listVmCmd)

        self.assertNotEqual(len(listVmResponse), 0, "Check if the list API \
                            returns a non-empty response")

        vm = listVmResponse[0]

        self.assertEqual(vm.id, deployVmResponse.id, "Check if the VM returned \
                         is the same as the one we deployed")


        self.assertEqual(vm.state, "Running", "Check if VM has reached \
                         a state of running")

    def tearDown(self):
        """
        And finally let us cleanup the resources we created by deleting the
        account. All good unittests are atomic and rerunnable this way
        """
        deleteAcct = deleteAccount.deleteAccountCmd()
        deleteAcct.id = self.acctResponse.account.id
        self.apiClient.deleteAccount(deleteAcct)

package com.amazonaws.samples;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClientBuilder;
import com.amazonaws.services.identitymanagement.model.AttachRolePolicyRequest;
import com.amazonaws.services.identitymanagement.model.AttachedPolicy;
import com.amazonaws.services.identitymanagement.model.CreateRoleRequest;
import com.amazonaws.services.identitymanagement.model.CreateRoleResult;
import com.amazonaws.services.identitymanagement.model.GetRolePolicyRequest;
import com.amazonaws.services.identitymanagement.model.GetRoleRequest;
import com.amazonaws.services.identitymanagement.model.GetRoleResult;
import com.amazonaws.services.identitymanagement.model.ListAttachedRolePoliciesRequest;
import com.amazonaws.services.identitymanagement.model.ListAttachedRolePoliciesResult;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AttachRolePolicy {

    static AmazonIdentityManagement iam;
	public static final String POLICY_ARN =
        "arn:aws:iam::aws:policy/AmazonDynamoDBFullAccess";
    
    private static void init() throws Exception {
  
        ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
        try {
            credentialsProvider.getCredentials();
            System.out.println("credentail loadeds");
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct " +
                    "location (C:\\Users\\syntel\\.aws\\credentials), and is in valid format.",
                    e);
        }
        iam = AmazonIdentityManagementClientBuilder.standard()
            .withCredentials(credentialsProvider)
            .withRegion("us-east-2")
            .build();
    }

    public static void main(String[] args) throws Exception {
    	init();
    	createIamRole();
    	//attatchPolicyToRole();
    }
    
    public static void createIamRole() {
    	String role_name = "testSdkRole";
    	String POLICY_DOCUMENT =
    	        "{\r\n" + 
    	        "  \"Version\": \"2012-10-17\",\r\n" + 
    	        "  \"Statement\": {\r\n" + 
    	        "    \"Effect\": \"Allow\",\r\n" + 
    	        "    \"Principal\": {\"Service\": \"ec2.amazonaws.com\"},\r\n" + 
    	        "    \"Action\": \"sts:AssumeRole\"\r\n" + 
    	        "  }\r\n" + 
    	        "}";
    	
    	GetRoleRequest req = new GetRoleRequest().withRoleName("SQSRole");
    	GetRoleResult res = iam.getRole(req);
    	System.out.println(res);   
    	
    	
    	CreateRoleRequest request2 = new CreateRoleRequest().withPath("/service-role/").withRoleName(role_name).withAssumeRolePolicyDocument(POLICY_DOCUMENT).withDescription("Created through AWS SDK");
        CreateRoleResult response2 = iam.createRole(request2);
        System.out.println("Role Created");
    	
    }
    
    
    /**
     * Attach policy to role
     */
    public static void attatchPolicyToRole() {
    	String role_name = "SQSRole";

        ListAttachedRolePoliciesRequest request =
            new ListAttachedRolePoliciesRequest()
                .withRoleName(role_name);
        
      

        List<AttachedPolicy> matching_policies = new ArrayList<>();

        boolean done = false;

        while(!done) {
            ListAttachedRolePoliciesResult response =
                iam.listAttachedRolePolicies(request);

            matching_policies.addAll(
                    response.getAttachedPolicies()
                            .stream()
                            .filter(p -> p.getPolicyName().equals(role_name))
                            .collect(Collectors.toList()));

            if(!response.getIsTruncated()) {
                done = true;
            }
            request.setMarker(response.getMarker());
        }

        if (matching_policies.size() > 0) {
            System.out.println(role_name +
                    " policy is already attached to this role.");
            return;
        }
        
        AttachRolePolicyRequest attach_request =
            new AttachRolePolicyRequest()
                .withRoleName(role_name)
                .withPolicyArn(POLICY_ARN);

        iam.attachRolePolicy(attach_request);

        System.out.println("Successfully attached policy " + POLICY_ARN +
                " to role " + role_name);
    }
}


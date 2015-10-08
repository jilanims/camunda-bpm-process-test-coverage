package org.camunda.bpm.extension.process_test_coverage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineTestCase;
import org.camunda.bpm.extension.process_test_coverage.ProcessTestCoverage;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.Process;

/**
 * Test case starting an in-memory database-backed Process Engine.
 */
public class ProcessTestCoverageTest extends ProcessEngineTestCase {

  private static final String PROCESS_DEFINITION_KEY = "process-test-coverage";
  
  @Override
  protected void tearDown() throws Exception {
    // calculate coverage for all tests
    ProcessTestCoverage.calculate(processEngine);
    // TODO identify for which method the tearDown is called, e.g. using: String testCaseName = this.getClass().getName() + "." + getName();
    super.tearDown();
  }

  @Deployment(resources = "process.bpmn")
  public void testPathA() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("path", "A");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY, variables);

    // calculate coverage for this method, but also add to the overall coverage of the process
    ProcessTestCoverage.calculate(processInstance, processEngine);
  }

  @Deployment(resources = "process.bpmn")
  public void testPathB() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("path", "B");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY, variables);
    
    // calculate coverage for this method, but also add to the overall coverage of the process
    ProcessTestCoverage.calculate(processInstance.getId(), processEngine);
  }
  
  @Deployment(resources = "transactionBoundaryTest.bpmn")
  public void testTxBoundaries() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("transactionBoundaryTest");
    
    // calculate coverage for this method, but also add to the overall coverage of the process
    ProcessTestCoverage.calculate(processInstance.getId(), processEngine);
  }

  @Deployment(resources = "collaboration-with-non-executable-process.bpmn")
  public void testCollaborationWithNonExecutableProcess() {
    String processDefinitionKey = "Process_1";
    String processDefinitionId = repositoryService.createProcessDefinitionQuery().processDefinitionKey(processDefinitionKey).latestVersion().singleResult().getId();
    BpmnModelInstance model = repositoryService.getBpmnModelInstance(processDefinitionId);
//    assertNull(model.getModelElementById("StartEvent_2"));
    Collection<Process> processes = model.getModelElementsByType(Process.class);
    Process theProcess = null;
    for (Process process : processes) {
      if (process.getId().equals(processDefinitionKey)) {
        theProcess = process;
        break;
      }
    }
    assertNotNull(theProcess);
    Collection<FlowNode> flowNodes = theProcess.getChildElementsByType(FlowNode.class);
    FlowNode startEvent3 = null;
    for (FlowNode flowNode : flowNodes) {
      if (flowNode.getId().equals("StartEvent_2")) {
        fail();
      }
      if (flowNode.getId().equals("StartEvent_3")) {
        startEvent3 = flowNode;
      }
    }
    assertNull(startEvent3); // TODO recurse into sub-processes
  }
  
}

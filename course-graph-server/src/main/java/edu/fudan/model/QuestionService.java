package edu.fudan.model;

import edu.fudan.domain.*;
import edu.fudan.dto.response.QuestionResp;
import edu.fudan.exception.NodeNotFoundException;
import edu.fudan.exception.PermissionDeniedException;
import edu.fudan.exception.QuestionNotFoundException;
import edu.fudan.repository.AnswerEntryRepository;
import edu.fudan.repository.NodeRepository;
import edu.fudan.repository.QuestionMultipleChoiceRepository;
import edu.fudan.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class QuestionService {

    private final NodeRepository nodeRepository;

    private final QuestionRepository questionRepository;

    private final QuestionMultipleChoiceRepository questionMultipleChoiceRepository;

    private final AnswerEntryRepository answerEntryRepository;

    private final PermissionService permissionService;

    @Autowired
    public QuestionService(NodeRepository nodeRepository, QuestionRepository questionRepository,
                           QuestionMultipleChoiceRepository questionMultipleChoiceRepository,
                           AnswerEntryRepository answerEntryRepository,
                           PermissionService permissionService) {
        this.nodeRepository = nodeRepository;
        this.questionRepository = questionRepository;
        this.questionMultipleChoiceRepository = questionMultipleChoiceRepository;
        this.answerEntryRepository = answerEntryRepository;
        this.permissionService = permissionService;
    }

    /**
     * List all questions of the user.
     * If the user is the teacher, include statistical information in response.
     * If the user is a student, include his answers in response. For question he has not answered, keep it null.
     * Otherwise, raise PermissionDeniedException
     *
     * @param currentUser, current login user
     * @param nodeId,      id of the node
     * @return list of question information
     */
    public List<QuestionResp> getAllQuestionsOfNode(User currentUser, String nodeId) {
        // Node must exist
        // To reach the courseId we set the depth to 2
        Node node = nodeRepository.findById(nodeId, 2).orElseThrow(
                NodeNotFoundException::new
        );

        // Check read permission
        if (!permissionService.checkReadPermOfCourse(currentUser, node.getCourseId())) {
            throw new PermissionDeniedException();
        }

        // Get all questions of the node
        List<Question> questions = node.getQuestionList();

        List<QuestionResp> questionRespList = new ArrayList<>();

        for (Question shallowQuestion : questions) {
            // Get all data related to the question, set depth to -1
            Question question = questionRepository.findById(shallowQuestion.getQuestionId(), -1).orElseThrow(
                    QuestionNotFoundException::new
            );
            QuestionResp questionResp = new QuestionResp(question, currentUser);
            questionRespList.add(questionResp);
        }

        return questionRespList;
    }

    public QuestionResp getQuestion(User currentUser, long questionId) {
        // Question must first exist
        Question question = questionRepository.findById(questionId, -1).orElseThrow(
                QuestionNotFoundException::new
        );

        // Check read permission
        if (!permissionService.checkReadPermOfCourse(currentUser, question.getCourseId())) {
            throw new PermissionDeniedException();
        }

        return new QuestionResp(question, currentUser);
    }

    /**
     * create a new question and add it to a course node
     *
     * @param nodeId      id of course node
     * @param description description of the question to be added
     * @param choices     choices of the question to be added
     * @param answer      the answer of this multiple-choice question
     * @return response information of the question created newly
     */
    public QuestionResp createQuestion(User currentUser, String nodeId, String description,
                                       QuestionType type, List<Choice> choices, String answer) {
        // Node must exist
        // To reach the courseId we set the depth to 2
        Node node = nodeRepository.findById(nodeId, 2).orElseThrow(
                NodeNotFoundException::new
        );

        long courseId = node.getCourseId();

        // Current user must have the write permission of the course
        if (!permissionService.checkWritePermOfCourse(currentUser, courseId)) {
            throw new PermissionDeniedException();
        }

        // Generate a new question id
        long questionId = RandomIdGenerator.getInstance().generateRandomLongId(questionRepository);

        Question question = null;

        switch (type) {
            case MULTIPLE_CHOICE: {
                question = new QuestionMultipleChoice(questionId, description, choices, answer, courseId);
                break;
            }
            case SHORT_ANSWER: {
                question = new QuestionShortAnswer(questionId, description, courseId);
                break;
            }
        }

        // Add the question to the node
        node.addQuestion(question);

        //save the changes in the database
        questionRepository.save(question);
        nodeRepository.save(node);

        // Return question response
        return new QuestionResp(question, currentUser);
    }

    void deleteQuestion(Question question) {
        if (question.getQuestionType() == QuestionType.MULTIPLE_CHOICE) {
            QuestionMultipleChoice questionMultipleChoice = questionMultipleChoiceRepository.findById(
                    question.getQuestionId()).orElseThrow(
                    QuestionNotFoundException::new
            );
            // Delete all the choices
            questionMultipleChoice.removeChoices();
            questionMultipleChoiceRepository.save(questionMultipleChoice);
        }

        questionRepository.delete(question);
    }

    /**
     * Delete a question.
     * Need to check permission.
     *
     * @param currentUser current login user
     * @param questionId  if of the question
     */
    public void deleteQuestion(User currentUser, long questionId) {
        // First question must exist
        Question question = questionRepository.findById(questionId, -1).orElseThrow(
                QuestionNotFoundException::new
        );

        // Check if the login user is the owner/teacher of the course
        if (!permissionService.checkWritePermOfCourse(currentUser, question.getCourseId())) {
            throw new PermissionDeniedException();
        }

        deleteQuestion(question);
    }

    /**
     * Student submit a answer
     *
     * @param currentUser, current login user
     * @param questionId,  id of the question
     * @param answer,      content of the answer
     */
    public void createAnswerEntry(User currentUser, long questionId, String answer) {
        // Question must exist
        Question question = questionRepository.findById(questionId).orElseThrow(
                QuestionNotFoundException::new
        );

        // Current user must be the student of the question
        if (!permissionService.checkReadPermOfCourse(currentUser, question.getCourseId())) {
            throw new PermissionDeniedException();
        }

        AnswerEntry answerEntry = new AnswerEntry(currentUser.getUserId(), question, answer);

        answerEntryRepository.save(answerEntry);
    }
}

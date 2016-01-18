package com.mapbar.neo4j.research;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;

/**
 * @author wupeng
 * @version 1.0
 * @date 2016-01-15
 * @modify
 * @copyright Navi Tsp
 */
public class HelloWord {

    public static final String db_path = "F:/temp2";

    public final GraphDatabaseService databaseService;

    public final Index<Node> index1;

    static final String USER_NAME_KEY = "user_name";

    public HelloWord() {
        databaseService = new GraphDatabaseFactory().newEmbeddedDatabase(db_path);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                databaseService.shutdown();
                System.out.println("graph database shutdown ... ");
            }
        });
        Transaction transaction = databaseService.beginTx();
        try {
            index1 = databaseService.index().forNodes("index1");
            transaction.success();
        } finally {
            transaction.close();
        }
    }

    void createNode() {
        Transaction transaction = databaseService.beginTx();

        try {
            Node firstNode = databaseService.createNode();
            firstNode.setProperty("name", "hello");
            index1.add(firstNode, USER_NAME_KEY, firstNode.getProperty("name"));

            Node secondNode = databaseService.createNode();
            secondNode.setProperty("name", "world !");
            index1.add(secondNode, USER_NAME_KEY, secondNode.getProperty("name"));

            Relationship relationship = firstNode.createRelationshipTo(secondNode, RelationShipTypes.KNOWS);
            relationship.setProperty("name", "I am relationship !");

            transaction.success();
        } catch (Exception e) {
            e.printStackTrace();
            transaction.failure();
        } finally {
            transaction.close();
        }
    }

    Node findUser(String name) {
        return index1.get(USER_NAME_KEY, name).getSingle();
    }

    void deleteUser(Node node) {
        Transaction transaction = databaseService.beginTx();
        try {
            index1.remove(node, USER_NAME_KEY, node.getProperty("name"));
            Relationship relationship = node.getSingleRelationship(RelationShipTypes.KNOWS, Direction.OUTGOING);
            if (relationship != null)
                relationship.delete();
            node.delete();
            transaction.success();
        } catch (Exception e) {
            e.printStackTrace();
            transaction.failure();
        } finally {
            transaction.close();
        }
    }

    public static void main(String[] args) {
        HelloWord helloWord = new HelloWord();
//        helloWord.createNode();

        Transaction transaction = helloWord.databaseService.beginTx();
        try {
            Node first = helloWord.findUser("hello");
            System.out.println(first.getProperty("name"));

            Node second = helloWord.findUser("world !");
            System.out.println(second.getProperty("name"));
        } finally {
            transaction.success();
            transaction.close();
        }

//        helloWord.deleteUser(first);
//        helloWord.deleteUser(second);

    }
}

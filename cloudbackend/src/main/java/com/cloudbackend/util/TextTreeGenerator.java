package com.cloudbackend.util;

import com.cloudbackend.entity.FileMetadata;
import com.cloudbackend.entity.User;
import com.cloudbackend.repository.FileMetadataRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TextTreeGenerator {
    private final FileMetadataRepository fileMetadataRepository;

    public TextTreeGenerator(FileMetadataRepository fileMetadataRepository) {
        this.fileMetadataRepository = fileMetadataRepository;
    }


    private String generateTextTree(List<String> paths) {
        TreeNode root = new TreeNode("/");

        for (String path : paths) {
            String[] parts = path.split("/");
            TreeNode current = root;
            for (String part : parts) {
                if (!part.isEmpty()) {
                    current = current.children.computeIfAbsent(part, TreeNode::new);
                }
            }
        }

        StringBuilder treeOutput = new StringBuilder();
        buildTreeString(root, "", treeOutput);
        return treeOutput.toString();
    }

    private void buildTreeString(TreeNode node, String prefix, StringBuilder output) {
        if (!node.name.equals("/")) {
            output.append(prefix).append("├── ").append(node.name).append("\n");
        }

        List<TreeNode> children = new ArrayList<>(node.children.values());
        for (int i = 0; i < children.size(); i++) {
            buildTreeString(children.get(i), prefix + (node.name.equals("/") ? "" : "    "), output);
        }
    }

    static class TreeNode {
        String name;
        Map<String, TreeNode> children = new HashMap<>();

        TreeNode(String name) {
            this.name = name;
        }
    }
}
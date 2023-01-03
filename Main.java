//ProjectMainBody

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.core.DockerClientBuilder;

//Spring MVC framework to handle the form submission and create a JSON file with the user input

//Uses Docker Java library to connect to the Docker daemon on the remote host and 
//creates a Docker container using the JSON file as the configuration

//Uses the Jackson library to create a JSON file with the user input,
//and then reads the JSON file to create a Docker container on the remote host using the Docker daemon.

@Controller
public class DockerController {
    @GetMapping("/")
    public String getForm(Model model) {
        model.addAttribute("containerForm", new ContainerForm());
        return "form";
    }

    @PostMapping("/")
    public String createContainer(ContainerForm containerForm) {
        // Create a JSON file with the user input
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(new File("container-config.json"), containerForm);

        // Connect to the Docker daemon on the remote host
        DockerClient dockerClient = DockerClientBuilder.getInstance("tcp://remote-host:2375").build();

        // Read the JSON file and use it to create a Docker container
        ContainerConfig config = mapper.readValue(new File("container-config.json"), ContainerConfig.class);
        ExposedPort exposedPort = ExposedPort.tcp(Integer.parseInt(config.getPortMapping().split(":")[0]));
        Ports portBindings = new Ports();
        portBindings.bind(exposedPort, Ports.Binding.bindPort(Integer.parseInt(config.getPortMapping().split(":")[1])));
        CreateContainerResponse container = dockerClient.createContainerCmd(config.getImageName())
            .withExposedPorts(exposedPort)
            .withPortBindings(portBindings)
            .exec();

        // Start the container
        dockerClient.startContainerCmd(container.getId()).exec();

        return "redirect:/";
    }
}

class ContainerForm {
    private String containerName;
    private String imageName;
    private String portMapping;

    // Getters and setters for the form fields
}

class ContainerConfig {
    private String containerName;
    private String imageName;
    private String portMapping;

    // Getters and setters for the config fields
}

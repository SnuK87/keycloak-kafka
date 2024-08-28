FROM mcr.microsoft.com/dotnet/sdk:6.0

RUN apt update
RUN apt install curl -y
RUN apt install python3 -y
RUN apt install jq -y
RUN apt install git -y

# Install Java 17
RUN git clone https://github.com/asdf-vm/asdf.git ~/.asdf --branch v0.10.2 
RUN ~/.asdf/bin/asdf plugin-add java 
RUN ~/.asdf/bin/asdf install java openjdk-17
RUN ~/.asdf/bin/asdf global java openjdk-17
RUN ln -s ~/.asdf/installs/java/openjdk-17/bin/java /usr/bin/java 

# Install git version tool
RUN dotnet tool install GitVersion.Tool --global --version 5.9.0
ENV PATH="$PATH:/root/.dotnet/tools"

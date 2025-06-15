
Trino History Server Helm Chart
===========

A Helm chart for deploying a Trino History Server with a Spring Boot backend and optional Trino Web UI


## Usage

[Helm](https://helm.sh) must be installed to use this chart.  
Refer to Helm's [documentation](https://helm.sh/docs/) for setup instructions.

### Step 1: Clone the Repository

```bash
git clone https://github.com/yardenc2003/trino-history-server.git
cd trino-history-server/helm/
```

### Step 2: Customize Configuration (Optional)

You can customize the deployment using a `values.yaml` file.  
Start by copying the default values:

```bash
cp values.yaml my-values.yaml
```

Edit `my-values.yaml` as needed for your environment, such as:
- storage type and credentials
- Trino authentication
- PVC size or ingress settings

### Step 3: Add Chart Repositories and Build Dependencies

If your chart depends on other charts (e.g., `webui` includes Trino as a subchart), make sure to add the necessary Helm repositories and build dependencies:

```bash
helm repo add trino https://trinodb.github.io/charts/
helm repo update
helm dependency build
```

### Step 4: Render Manifests (Optional)

To inspect the generated Kubernetes manifests before applying:

```bash
helm template trino-history . -f my-values.yaml --namespace trino-history
```

### Step 5: Install the Chart

Install the chart using:

```bash
helm install trino-history . -f my-values.yaml
```


## Documentation

For more information about the *Trino History Server* Project and its configuration, refer the [project documentation](./../../README.md).
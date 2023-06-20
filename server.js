const express = require('express');
const path = require('path');

const app = express();

// Enable CORS for all routes
app.use(cors());

const distPath = path.join(__dirname, 'Frontend', 'dist', 'frontend');

// Set cache control headers for static files
app.use(express.static(distPath, { setHeaders: (res, filePath) => {
  if (filePath.endsWith('.js') || filePath.endsWith('.css')) {
    res.setHeader('Cache-Control', 'no-store');
  }
}}));

// Define the middleware function
const setJsonContentType = (req, res, next) => {
  res.setHeader('Content-Type', 'application/json');
  next(); // Call next to proceed to the next middleware or route handler
};

// Apply the middleware to all API calls
app.use('/', setJsonContentType);

const port = process.env.PORT || 8080;
app.listen(port, () => {
  console.log('Server is running on port', port);
});

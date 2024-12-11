import base64
import io
from flask import Flask, logging, request, jsonify
import torch
from PIL import Image
import numpy as np
from OCR.document_OCR.dan.trainer_dan import Manager
from basic.utils import pad_images

import os.path
import sys
import torch
from torch.optim import Adam
from PIL import Image
import numpy as np

from basic.models import FCN_Encoder
from OCR.document_OCR.dan.models_dan import GlobalHTADecoder
from OCR.document_OCR.dan.trainer_dan import Manager
from basic.utils import pad_images
from basic.metric_manager import keep_all_but_tokens
import logging

device = torch.device("cuda" if torch.cuda.is_available() else "cpu")

model_path = "dan_rimes_page.pt"

manager = None

class FakeDataset:
    def __init__(self, charset):
        self.charset = charset
        self.tokens = {
            "end": len(self.charset),
            "start": len(self.charset) + 1,
            "pad": len(self.charset) + 2,
        }


def get_params(weight_path):
    return {
        "dataset_params": {"charset": None},
        "model_params": {
            "models": {
                "encoder": FCN_Encoder,
                "decoder": GlobalHTADecoder,
            },
            "transfer_learning": {
                "encoder": ["encoder", weight_path, True, True],
                "decoder": ["decoder", weight_path, True, False],
            },
            "transfered_charset": True,
            "additional_tokens": 1,
            "input_channels": 3,
            "dropout": 0.5,
            "enc_dim": 256,
            "nb_layers": 5,
            "h_max": 500,
            "w_max": 1000,
            "l_max": 15000,
            "dec_num_layers": 8,
            "dec_num_heads": 4,
            "dec_res_dropout": 0.1,
            "dec_pred_dropout": 0.1,
            "dec_att_dropout": 0.1,
            "dec_dim_feedforward": 256,
            "use_2d_pe": True,
            "use_1d_pe": True,
            "use_lstm": False,
            "attention_win": 100,
        },
        "training_params": {
            "output_folder": "dan_rimes_page",
            "max_nb_epochs": 50000,
            "max_training_time": 3600 * 24 * 1.9,
            "load_epoch": "last",
            "interval_save_weights": None,
            "batch_size": 1,
            "valid_batch_size": 4,
            "use_ddp": False,
            "ddp_port": "20027",
            "use_amp": False,  # Disable AMP for CPU
            "nb_gpu": 1,  # Set number of GPUs to 0
            "ddp_rank": 0,
            "lr_schedulers": None,
            "eval_on_valid": True,
            "eval_on_valid_interval": 5,
            "focus_metric": "cer",
            "expected_metric_value": "low",
            "eval_metrics": ["cer", "wer", "map_cer"],
            "force_cpu": False,  # Force CPU
            "max_char_prediction": 3000,
            "teacher_forcing_scheduler": {
                "min_error_rate": 0.2,
                "max_error_rate": 0.2,
                "total_num_steps": 5e4,
            },
            "optimizers": {
                "all": {
                    "class": Adam,
                    "args": {
                        "lr": 0.0001,
                        "amsgrad": False,
                    }
                },
            },
        },
    }


def load_model():
    global manager
    params = get_params(model_path)  # Load model parameters

    # Ensure 'device' is defined
    checkpoint = torch.load(model_path, map_location=device)  # Use the device for loading the checkpoint

    charset = checkpoint.get("charset")
    if charset is None:
        raise ValueError("Checkpoint is missing 'charset'. Ensure the model checkpoint is correct.")

    params["model_params"]["vocab_size"] = len(charset)  # Set vocab_size
    logging.info(f"Vocab size set to: {params['model_params']['vocab_size']}")

    manager = Manager(params)
    manager.params["model_params"]["vocab_size"] = len(charset)  # Redundant but ensures compatibility
    manager.load_model()

    # Move models to the appropriate device
    for model_name in manager.models.keys():
        manager.models[model_name] = manager.models[model_name].to(device)
        manager.models[model_name].eval()

    manager.dataset = FakeDataset(charset)
    logging.info(f"Model loaded successfully on {device}")


def predict(image_path):
    # Load the image
    img = np.array(Image.open(image_path))
    img = np.expand_dims(img, axis=2) if len(img.shape) == 2 else img
    img = np.concatenate([img, img, img], axis=2) if img.shape[2] == 1 else img
    img = img[:, :, :3] if img.shape[2] == 4 else img  # Ensure only 3 channels

    # Preprocess the image
    imgs = pad_images([img], padding_value=0, padding_mode="br")
    imgs = torch.tensor(imgs).float().permute(0, 3, 1, 2).to(device)

    batch_data = {
        "imgs": imgs,
        "imgs_reduced_shape": [[img.shape[0] // 32, img.shape[1] // 8]],
        "imgs_position": [([0, img.shape[0]], [0, img.shape[1]])],
        "raw_labels": None,
    }

    with torch.no_grad():
        res = manager.evaluate_batch(batch_data, metric_names=[])
    prediction = res["str_x"]
    layout_tokens = "".join(['Ⓑ', 'Ⓞ', 'Ⓟ', 'Ⓡ', 'Ⓢ', 'Ⓦ', 'Ⓨ', "Ⓐ", "Ⓝ", 'ⓑ', 'ⓞ', 'ⓟ', 'ⓡ', 'ⓢ', 'ⓦ', 'ⓨ', "ⓐ", "ⓝ"])
    prediction = [keep_all_but_tokens(x, layout_tokens) for x in prediction]
    print(prediction)

if __name__ == "__main__":
    print(f"Current working directory: {os.getcwd()}")
    load_model()
    if len(sys.argv) != 2:
        print("Usage: python3 dan_server.py <image_path>")
        sys.exit(1)
    predict(sys.argv[1])
